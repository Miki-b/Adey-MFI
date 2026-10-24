#!/usr/bin/env bash
# Runs on EVERY Codespace start (postStartCommand), detached:
#   1. db + Fineract (never web-app), Fineract/Postgres bound to 127.0.0.1 only
#   2. wait until Fineract is healthy (first start: database migrations, several minutes)
#   3. seed demo data (idempotent: a second run changes nothing)
#   4. backend in the background, -Xmx512m, log in logs/backend.log
# Progress: logs/startup.log. State for scripts: logs/stack.status (STARTING/READY/FAILED: ...).
set -uo pipefail
cd "$(dirname "$0")/.."
REPO="$(pwd)"
mkdir -p logs
STATUS="$REPO/logs/stack.status"
exec >> "$REPO/logs/startup.log" 2>&1

T0=$(date +%s)
log() { echo "[$(date '+%H:%M:%S')] (+$(( $(date +%s) - T0 ))s) $*"; }
fail() { log "FAILED: $*"; echo "FAILED: $*" > "$STATUS"; exit 1; }

# Only one copy at a time (a restart while a previous run is still going).
exec 9> /tmp/adey-start-stack.lock
flock -n 9 || { log "another start-stack.sh is running; exiting"; exit 0; }

echo "STARTING" > "$STATUS"
log "===== starting Adey MFI stack ====="

if [ -z "${ADEY_JWT_SECRET:-}" ]; then
  fail "Codespaces secret ADEY_JWT_SECRET is not set for this repo (gh secret set ADEY_JWT_SECRET --user --app codespaces --repos <owner>/<repo>), then rebuild/restart the Codespace"
fi
for s in CHAPA_SECRET_KEY CHAPA_WEBHOOK_SECRET; do
  if [ -n "${!s:-}" ]; then log "secret $s present (reserved for the Chapa provider, not used yet)"; else log "secret $s not set (fine: simulator is the provider)"; fi
done

log "waiting for the Docker engine"
for i in $(seq 1 90); do docker info > /dev/null 2>&1 && break; sleep 2; done
docker info > /dev/null 2>&1 || fail "Docker engine not available"

log "docker compose up -d db fineract (web-app is NOT started)"
(cd infra && docker compose -f docker-compose.yml -f docker-compose.codespaces.yml up -d db fineract) \
  || fail "docker compose up failed"

log "waiting for Fineract health (first start runs migrations: 2-6 min)"
for i in $(seq 1 180); do
  if curl -sk --max-time 5 https://127.0.0.1:8443/fineract-provider/actuator/health | grep -q '"UP"'; then
    log "Fineract is UP"; break
  fi
  [ $(( i % 12 )) -eq 0 ] && log "  still waiting for Fineract..."
  sleep 5
done
curl -sk --max-time 5 https://127.0.0.1:8443/fineract-provider/actuator/health | grep -q '"UP"' || fail "Fineract did not become healthy"

JAR="$(ls backend/target/mfi-backend-*.jar 2>/dev/null | grep -v original | head -1)"
if [ -z "$JAR" ]; then
  log "backend jar missing; building it"
  (cd backend && ./mvnw -B -q package -DskipTests) || fail "backend build failed"
  JAR="$(ls backend/target/mfi-backend-*.jar | grep -v original | head -1)"
fi

log "seeding demo data (profile seed, idempotent)"
if java -Xmx512m -jar "$JAR" --spring.profiles.active=local,codespaces,seed > logs/seed.log 2>&1; then
  log "seed finished: $(grep -o 'Seeding finished: .*' logs/seed.log | tail -1)"
else
  fail "seed failed, see logs/seed.log"
fi

if pgrep -f "mfi-backend.*\.jar" > /dev/null && ! pgrep -f "profiles.active=.*seed" > /dev/null; then
  log "backend already running"
else
  log "starting backend (-Xmx512m), log: logs/backend.log"
  nohup java -Xmx512m -jar "$JAR" > logs/backend.log 2>&1 &
fi

for i in $(seq 1 60); do
  if curl -s --max-time 3 http://127.0.0.1:8080/actuator/health | grep -q '"UP"'; then
    log "backend is UP on port 8080"
    echo "READY" > "$STATUS"
    log "===== stack ready in $(( $(date +%s) - T0 ))s ====="
    exit 0
  fi
  sleep 3
done
fail "backend did not answer /actuator/health, see logs/backend.log"

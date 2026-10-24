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

# Codespaces secrets are in the environment of lifecycle commands (postStart), but NOT in a manual
# `gh codespace ssh` session. Fall back to the Codespace's secrets file (values are base64-encoded).
# Only the variables we need are read; values are never printed.
SECRETS_FILE=/workspaces/.codespaces/shared/.env-secrets
for v in ADEY_JWT_SECRET CHAPA_SECRET_KEY CHAPA_WEBHOOK_SECRET CODESPACE_NAME GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN; do
  if [ -z "${!v:-}" ] && [ -r "$SECRETS_FILE" ]; then
    enc="$(grep -m1 "^$v=" "$SECRETS_FILE" | cut -d= -f2-)"
    if [ -n "$enc" ]; then
      export "$v=$(printf %s "$enc" | base64 -d 2>/dev/null)"
      log "loaded $v from the Codespace secrets file"
    fi
  fi
done

# No JWT secret (e.g. a visitor who opened the "Open in Codespaces" badge): make a random one for
# this start only. Tokens then stop working after a restart, which is fine for a demo. Never printed.
if [ -z "${ADEY_JWT_SECRET:-}" ]; then
  export ADEY_JWT_SECRET="$(head -c 48 /dev/urandom | base64 | tr -d '\n')"
  log "ADEY_JWT_SECRET not set: generated an ephemeral JWT secret (valid until this Codespace restarts)"
fi

# Mobile money provider: Chapa (test mode) when its secret key exists, otherwise the TelePay simulator.
if [ -n "${MOBILE_MONEY_PROVIDER:-}" ]; then
  log "mobile money provider: $MOBILE_MONEY_PROVIDER (set explicitly)"
elif [ -n "${CHAPA_SECRET_KEY:-}" ]; then
  export MOBILE_MONEY_PROVIDER=chapa
  log "mobile money provider: chapa (CHAPA_SECRET_KEY present; the backend refuses non-test keys)"
  if [ -n "${CHAPA_WEBHOOK_SECRET:-}" ]; then log "CHAPA_WEBHOOK_SECRET present: signed Chapa webhooks accepted"
  else log "CHAPA_WEBHOOK_SECRET not set: Chapa webhooks refused, payments complete through verify polling"; fi
else
  export MOBILE_MONEY_PROVIDER=simulator
  log "CHAPA_SECRET_KEY not set: falling back to the TelePay simulator as mobile money provider"
fi

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

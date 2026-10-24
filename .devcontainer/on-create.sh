#!/usr/bin/env bash
# Runs ONCE when the Codespace is created: build the backend jar (no tests) and pre-pull the
# Docker images, so every later start only has to start things.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p logs
log() { echo "[$(date '+%H:%M:%S')] on-create: $*" | tee -a logs/startup.log; }

log "building backend (mvnw package -DskipTests)"
(cd backend && chmod +x mvnw && ./mvnw -B -q package -DskipTests)
log "backend jar: $(ls backend/target/mfi-backend-*.jar | grep -v original)"

log "waiting for the Docker engine (docker-in-docker)"
for i in $(seq 1 60); do docker info > /dev/null 2>&1 && break; sleep 2; done
log "pulling images for db and fineract"
(cd infra && docker compose -f docker-compose.yml -f docker-compose.codespaces.yml pull db fineract) || log "pull failed (will retry on start)"
log "done"

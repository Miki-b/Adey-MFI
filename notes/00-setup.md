# 00 — Setup notes

Date: 2026-10-02
Host: Windows 10 Pro, Docker Desktop 4.76.0 (WSL 2 backend), Docker Engine 29.5.2

## What was set up

A three-service Docker Compose stack under `adey-mfi/infra/`:

- **db** — `postgres:18.3`. Named volume `pgdata` mounted at
  `/var/lib/postgresql` (the Postgres 18 data layout). `init-db.sql` runs once on
  first init to create the `fineract_tenants` and `fineract_default` databases.
  Healthcheck via `pg_isready`.
- **fineract** — `apache/fineract:latest`, HTTPS on 8443, waits for the db to be
  healthy before starting. Configured via `FINERACT_*` env vars to use Postgres,
  tenant identifier `default`, timezone `Africa/Addis_Ababa`, JVM heap capped at
  1 GB.
- **web-app** — `openmf/web-app:1.14`, served on http://localhost:4200, pointed
  at the Fineract API on https://localhost:8443.

Folder structure created: `infra/`, `backend/`, `app/`, `calc/`, `postman/`,
`notes/` (the last five are empty placeholders for now).

The leftover `web-app` git-clone folder was left untouched.

## Problems hit and how they were solved

### 1. `openmf/web-app:latest` does not exist
The first `docker compose pull` failed with:
`docker.io/openmf/web-app:latest: not found`.

Checking Docker Hub showed the `openmf/web-app` repository has **no `latest`
tag** — only `dev` (a rolling build), `1.14` (newest numbered release), and many
`dev-<sha>` pins. Chose `1.14` (stable, reproducible) and updated the compose
file. This was a genuine "image reference doesn't resolve" issue, not slowness.

### 2. Pull timed out partway (slow connection)
A later pull failed mid-download with `net/http: TLS handshake timeout` on a
Fineract layer — the expected symptom of the ~40 KB/s connection. Fix: simply
re-ran `docker compose pull`. Docker keeps already-downloaded layers, so the
retry resumed rather than starting over. One retry was enough. Images were **not**
swapped to work around the slowness.

### 3. First Fineract start is slow (expected, not an error)
On first boot Fineract runs Liquibase migrations to create all its tables, which
takes a few minutes. Startup logs also print a harmless warning:
`could not create Vfs.Dir from url ... [file:/app/plugins/*]` — this is cosmetic
(no plugins directory) and can be ignored.

## Verification (all passed)

- `curl -k https://localhost:8443/fineract-provider/actuator/health`
  → `{"groups":["liveness","readiness"],"status":"UP"}`
- `curl -k https://localhost:8443/fineract-provider/api/v1/offices -H "Fineract-Platform-TenantId: default" -u mifos:password`
  → returns `Head Office` (id 1)
- `http://localhost:4200` → HTTP 200

## Credentials / key facts

- UI login: `mifos` / `password`, tenant `default`
- Postgres: `postgres` / `postgres`
- Before first UI login: open the health URL in the browser and accept the
  self-signed certificate.
### 4. postman setup issues on authorization header setup the postman doesnt have by default so need to prepare a script for that
## 
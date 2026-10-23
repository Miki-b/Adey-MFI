-- Runs ONCE, when the Postgres volume is first created.
-- Fineract's own databases:
CREATE DATABASE fineract_tenants;
CREATE DATABASE fineract_default;

-- Our backend's own database (refresh tokens, audit log). A separate login that owns only this
-- database, so the backend has no access to Fineract's tables. Local password only.
CREATE ROLE adey LOGIN PASSWORD 'adey';
CREATE DATABASE adey_backend OWNER adey;

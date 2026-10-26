# Proposal

## Why

Two operator-observable behavior areas have no capability spec: the API gateway (routing, per-IP rate limiting, transparent-proxy guarantees) and service logging (profile-based output, retention, the in-memory recent-entries buffer, and the admin-only logs endpoints). `AGENTS.md` documents both informally, but nothing in `openspec/specs/` contracts them — so changes to rate limits, routes, or log access cannot be checked against a behavior baseline. A framework audit also showed that specs seeded without a change history are exactly where spec/code drift accumulates; this change therefore documents the behavior through the normal propose → archive flow.

## What Changes

- Add an `api-gateway` capability spec capturing existing, verified behavior: path-based routing to auth-service and main-service, per-IP rate limiting with the configured limits and rejection status, transparent proxying (no gateway-level authentication), and the cross-origin header handling the gateway performs before forwarding.
- Add a `logging` capability spec capturing existing, verified behavior: the three logging profiles and what each emits, file retention limits, the in-memory buffer of recent entries (capacity and eviction), and admin-only access to the buffered entries in both services.
- No code changes. Every requirement is verified against the current implementation before archive; any requirement the code does not actually satisfy is reported and removed from the delta rather than silently specified.

Out of scope: fixing any behavior found during verification (that would be its own change), test backfill for existing capabilities, and metrics/tracing/health-check behavior.

## Capabilities

### New Capabilities

- `api-gateway`: how client traffic is routed between the SPA and backend services, how request volume is limited per client IP, and the transparency guarantees the gateway makes about authentication and forwarded requests.
- `logging`: how the services emit logs per deployment profile, how long file logs are retained, how many recent entries each service keeps in memory, and who may read those entries.

### Modified Capabilities

_None — no existing requirement changes. The `auth` spec's administrator-authorization requirement continues to guard `/auth/admin/**`; the `logging` spec adds the main-service side and the buffer behavior without altering it._

## Impact

`openspec/specs/` only (two new spec files merged at archive). No code, API, database, dependency, or deployment changes — `docker-compose.yml`, `render.yaml`, and the Dockerfiles are untouched. Conventions stay in `AGENTS.md` per `openspec/config.yaml`.

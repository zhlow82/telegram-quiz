# Design

## Context

See `proposal.md` — Why. The backend already has `spring-boot-starter-test`; the frontend has no test runner. The specs in `openspec/specs/` already describe the behavior to cover, so the tests are derived from those scenarios rather than invented.

## Goals / Non-Goals

**Goals:**
- A local `mvn test` that runs without Docker and covers pure logic and controller slices.
- A `mvn verify` that runs repository and integration tests against real PostgreSQL and Redis.
- A `npm run test:unit` that covers components, stores, and the API client.
- CI that runs all of the above.

**Non-Goals:**
- Refactoring the Telegram bot for testability (Phase 3).
- Playwright end-to-end tests (Phase 5).
- Coverage thresholds or mutation testing.
- Testing the Telegram network or a real bot token.

## Decisions

### Testcontainers, not H2
The schema uses JSONB, `TIMESTAMPTZ`, and PostgreSQL defaults. H2 would accept a different schema and hide production failures. Use `postgres:17-alpine` and `redis:7-alpine`, with Flyway running the real migrations.

### Surefire for `*Test`, Failsafe for `*IT`
`mvn test` stays fast and Docker-free. `mvn verify` adds `integration-test` and `verify` goals for `*IT.java`. CI uses `mvn verify`.

### Singleton containers per test JVM
Start the PostgreSQL and Redis containers once per module test run via static containers and `@DynamicPropertySource`. Per-class containers are too slow (each Flyway run costs seconds).

### Test names carry the spec trace
Each test class references the capability at the top, and each test names the requirement/scenario it covers, e.g. `@DisplayName("auth / Password sign-in / Deactivated account")`. No separate traceability matrix.

### Vitest + jsdom for the frontend
Vue Test Utils and `AppDialog`'s focus trap need a DOM with focus support; jsdom is the safer choice over happy-dom. `axios-mock-adapter` covers the `api.ts` interceptor because it does not require a network layer.

### Mock the seams, not the data
Unit tests mock repositories and external services. Repository tests use the real database and real queries — that is where the access-control risk lives.

## Risks / Trade-offs

- **Docker required for `mvn verify`.** Local runs fail loudly if Docker is absent; the README/AGENTS.md will say so. Unit and frontend tests remain Docker-free.
- **Container startup cost.** Singleton containers add ~5–10 seconds per module JVM; acceptable for `verify`, avoided by `test`.
- **`api.ts` module state.** `isRefreshing` and the subscriber queue are module-level, so tests must `vi.resetModules()` between cases and stub `window.location`.
- **Seeded `localadmin`.** `DataInitializer` runs in integration tests; use it intentionally for the auth flow rather than mocking the user store.

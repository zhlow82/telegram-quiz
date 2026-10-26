# Proposal

## Why

The project has 62 behavior requirements and no automated tests. Security, ownership, and quiz-activation rules are only verified manually, so regressions stay invisible until runtime.

## What Changes

- Add Testcontainers-based backend infrastructure (PostgreSQL, Redis) and split fast tests from integration tests with Surefire/Failsafe.
- Add JUnit 5 + Mockito + AssertJ tests for JWT claims, bot-token encryption, folder access, quiz activation, question access, and the auth flow.
- Add Vitest + Vue Test Utils infrastructure and tests for `AppDialog`, the auth store, and the API client interceptors.
- Add a GitHub Actions workflow running backend and frontend checks.
- Document test commands and conventions in `AGENTS.md` and a new `testing-conventions` skill.

## Capabilities

### New Capabilities

_None — test tooling only._

### Modified Capabilities

_None — no behavior changes._

## Impact

Test-scope dependencies only; `spring-boot-starter-test` is already present in every service. No runtime, database, or deployment changes: `docker-compose.yml`, `render.yaml`, and the Dockerfiles are untouched. Integration tests require Docker Desktop locally; CI runners provide Docker.

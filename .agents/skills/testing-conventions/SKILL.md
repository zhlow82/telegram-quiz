---
name: testing-conventions
description: Use when creating or modifying automated tests in this project. Covers the backend JUnit/Testcontainers stack, frontend Vitest setup, test naming and spec traceability, and what to mock.
---

# Testing Conventions

> Scope: implementation patterns only. Behavior contracts live in `openspec/specs/`; the propose → apply → archive workflow is in `AGENTS.md`.

## Stack

| Layer | Tooling | Command |
|---|---|---|
| Backend unit / slice | JUnit 5, Mockito, AssertJ, MockMvc | `mvn test` |
| Backend repository / integration | Testcontainers PostgreSQL + Redis, Failsafe | `mvn verify` |
| Frontend component / store / service | Vitest, Vue Test Utils, jsdom, axios-mock-adapter | `npm run test:unit` |
| Type check / build | `vue-tsc`, Vite | `npx vue-tsc --noEmit`, `npm run build` |

`mvn test` must stay Docker-free. `*IT.java` runs only on `mvn verify` and requires Docker Desktop. The frontend has no Docker dependency.

## File placement

```
src/backend/<service>/src/test/java/com/telegramquiz/<service>/
├── <layer>/<Subject>Test.java     # unit or slice (*Test)
├── repository/<Subject>IT.java   # Testcontainers (*IT)
├── integration/<Flow>IT.java     # full context (*IT)
└── support/                      # containers, factories, token helpers

src/frontend/src/<area>/__tests__/<Subject>.spec.ts
```

## Naming and traceability

Name tests after the capability and scenario they cover:

```java
@Test
@DisplayName("auth / Password sign-in / Deactivated account")
void deactivatedAccountCannotSignIn() { ... }
```

```ts
it('auth / Password sign-in / stores the session after login', async () => { ... })
```

Reference the capability path at the top of the class when the mapping is not obvious. Do not maintain a separate traceability document.

## Rules

- **Do not run tests unless explicitly asked.** Creating and updating test cases is still required; execution is opt-in. CI is the authoritative run.
- **No H2.** Repository tests use `postgres:17-alpine`; JSONB and PostgreSQL defaults are part of the behavior.
- **Do not mock the database in repository tests.** Mock repositories only in unit tests.
- **Do not test private methods.** Extract a seam or test through the public behavior.
- **Do not test the Telegram network.** Mock `TelegramBotManager`; the bot flow gets its own extracted engine in a later change.
- **Keep `mvn test` fast.** Anything needing a container is `*IT` and tagged by naming, not by a system property.
- **Frontend tests use the real Pinia store and a mocked service layer**, not a mocked store.
- **One behavior per test.** Multiple assertions are fine when they describe the same outcome.

## Current coverage

Phase 1 covers JWT claims, bot-token encryption, folder access levels, quiz activation/token conflicts, question access queries + JSONB, the auth flow, `AppDialog`, the auth store, and the API client. Add tests to the same classes when a change touches those behaviors.

Deferred coverage: `api-gateway` and `logging` scenarios — the gateway module has no test sources or test infrastructure, and the log buffer and admin logs endpoints are untested. These scenarios gain coverage in a dedicated gateway/logging test-infrastructure change (deferral recorded per the coverage-ratchet rule in `openspec/config.yaml`).

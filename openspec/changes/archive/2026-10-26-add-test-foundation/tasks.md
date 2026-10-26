# Tasks

## 1. Backend infrastructure

- [x] 1.1 Add `spring-boot-testcontainers`, `testcontainers:junit-jupiter`, and `testcontainers:postgresql` test dependencies to auth-service and main-service, and verify `mvn -q test` resolves them.
- [x] 1.2 Add `maven-failsafe-plugin` to both services and verify `mvn verify` runs `*IT.java` after `*Test.java`.
- [x] 1.3 Add shared PostgreSQL/Redis container bases and `TestDataFactory` helpers under `src/test/java`, and verify one repository test starts the container.
- [x] 1.4 Add `src/test/resources/application-test.yml` for JWT and bot-token test keys, and verify the context loads.

## 2. Backend tests

- [x] 2.1 `JwtUtilTest` covering access/refresh type claims, expiry, and invalid tokens.
- [x] 2.2 `BotTokenEncryptionServiceTest` covering round-trip and tampered ciphertext.
- [x] 2.3 `FolderAccessTest` covering OWNER, CO_OWNER, CONTRIBUTOR, NONE, and pending membership.
- [x] 2.4 `QuizActivationTest` covering one-token conflict, token masking, and start/stop bot calls.
- [x] 2.5 `QuestionAccessIT` covering owner, accepted share, pending share, no access, and JSONB round-trip.
- [x] 2.6 `AuthFlowIT` covering login, refresh, logout, deactivated account, and the admin route guard.

## 3. Frontend infrastructure

- [x] 3.1 Add Vitest, Vue Test Utils, jsdom, and axios-mock-adapter; add `test:unit` and `test:unit:watch` scripts.
- [x] 3.2 Add `vitest.config.ts` and `src/test/setup.ts` (localStorage, `window.location`, module reset); verify a trivial test runs.

## 4. Frontend tests

- [x] 4.1 `AppDialog.spec.ts`: backdrop does not cancel; X, Cancel, and Escape do.
- [x] 4.2 `auth.spec.ts`: login persists tokens/role; logout clears state on API failure; displayName fallback.
- [x] 4.3 `api.spec.ts`: 401 refresh and retry; refresh failure clears session; parallel 401s share one refresh.

## 5. CI and documentation

- [x] 5.1 Add `.github/workflows/ci.yml` with the backend matrix (`mvn -B verify`) and frontend checks.
- [x] 5.2 Add a `## Testing` section to `AGENTS.md` with commands and the Docker requirement.
- [x] 5.3 Add `.agents/skills/testing-conventions/SKILL.md` with the stack, file naming, and mocking rules.
- [x] 5.4 Add test rules to `openspec/config.yaml` (`tasks` and `apply` guidance).

## 6. Verification

- [x] 6.1 `cd src/backend/auth-service && mvn test` and `main-service` pass without Docker.
- [x] 6.2 `cd src/backend/auth-service && mvn verify` and `main-service` pass with Docker Desktop running.
- [x] 6.3 `cd src/frontend && npm run test:unit -- --run` passes.
- [x] 6.4 `cd src/frontend && npx vue-tsc --noEmit` and `npm run build` pass.
- [x] 6.5 `openspec validate --all --strict` passes, then archive this change.

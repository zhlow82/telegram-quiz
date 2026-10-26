# Design

## Context

See proposal.md — Why. Current constraints that shape the approach:

- Existing test infra is minimal but sufficient: `support/TestContainers.java` starts one shared `postgres:17-alpine` + `redis:7-alpine` per JVM; IT classes opt in via their own `@DynamicPropertySource` calling `TestContainers.register(registry)`. There is no test-resources profile — tests run against the main `application.yml` (dev JWT secret, 24h/7d expirations, `app.frontend-url=http://localhost:5173`).
- Shared containers mean Spring contexts are cached and **data persists across IT classes**: unique usernames (UUID suffix) are mandatory; there is no cleanup infrastructure. `DataInitializer` runs on every context start, so `localadmin`/`password88` is always available.
- Spring Boot 4: MockMvc auto-config import is `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` (not the Boot-3 package). Failsafe defaults pick up `*IT`; Surefire picks up `*Test`.
- Exception→status mapping (`GlobalExceptionHandler`): `IllegalArgumentException`→400, `IllegalStateException`→409, Spring `AuthenticationException`→401, plain `RuntimeException`→500. Admin "entity not found" paths throw plain `RuntimeException` → tests must expect **500**, not 404, there.

## Goals / Non-Goals

**Goals:**
- Every one of the 36 `auth` spec scenarios is either covered by an existing test, a new test, or explicitly strengthened from partial to full coverage.
- Tests assert spec'd behavior as-is (code is source of truth where spec and code agree — zero drift was verified in the audit).
- Each test's `@DisplayName` follows `auth / <Requirement name> / <scenario detail>` so coverage is traceable to `openspec/specs/auth/spec.md`.

**Non-Goals:**
- No production code changes, even where a test could arguably justify one.
- No test for the Telegram network, gateway, or other capabilities.
- No new shared test fixtures/helpers beyond what each class needs locally (no premature `TestDataFactory`).

## Decisions

1. **Class split mirrors spec requirement clusters** (7 classes), so a failing test points at one requirement area:
   - `integration/AuthLoginIT` — sign-in outcomes, logout without token, refresh rotation across devices, revoked/never-stored refresh tokens, expired bearer end-to-end (scenarios 1.1, 1.2, 2.2, 3.2, 3.3, 5.2).
   - `integration/ProfileAndPasswordIT` — profile read/clear-name, password change success/wrong-current/Google-account (7.1–7.3, 8.1–8.2).
   - `integration/AdminUserManagementIT` — self-delete guard, Google password-reset 409, deactivate blocks login + stored refresh, role change reflected in next refresh (13.1–13.3, 3.1).
   - `integration/GoogleRegistrationIT` — configured flag, admin google-settings read/save-without-secret, invitation completion/expired-state/invalid-code/already-registered, code reuse + deactivation (9.1–9.3, 11.1–11.4, 12.1–12.2).
   - `integration/BrandingSettingsIT` — default branding before any admin write, custom logo URL after (15.1–15.2).
   - `security/OAuth2SuccessHandlerTest` (unit) — registered-user redirect with minted token; unknown-user pending-registration Redis write + 10-min TTL + register redirect (10.1, 10.2).
   - `config/DataInitializerTest` (unit) — first-startup seeding; role-restore leaves password hash untouched (14.1, 14.2).
2. **Unit over IT where no HTTP/JPA boundary is crossed.** `OAuth2SuccessHandler` and `DataInitializer` are pure logic over injected dependencies → Mockito unit tests (fast, no containers). Alternative (drive via full context) rejected: OAuth2 login chain can't be triggered without a real Google handshake.
3. **OAuth2 pending state is seeded directly in Redis** via autowired `StringRedisTemplate` (`opsForHash().putAll("oauth2:pending:<state>", Map.of("sub",..., "email",..., "firstName",..., "lastName",...))`). This tests the registration endpoint's contract without any Google network call — consistent with the "never test the Telegram network" rule generalized to third-party networks.
4. **Google-user auth in ITs mints tokens directly** via autowired `JwtUtil.generateAccessToken(username, id, role, "google")` rather than going through the OAuth2 login chain.
5. **Ordering only where state genuinely precedes**: `GoogleRegistrationIT` (configured=false must run before google settings are saved) and `BrandingSettingsIT` (defaults must be read before the admin write) use `@TestMethodOrder(OrderAnnotation)`. Everything else is order-independent via unique usernames/states.
6. **Expired-token end-to-end** (2.2): craft an expired JWT with a locally constructed `JwtUtil` (same Base64 dev secret, `expiration=-1000` via `ReflectionTestUtils`) and present it as Bearer → expect 403 from the filter chain (unauthenticated on a protected route). Alternative (mutating the shared autowired bean's expiration) rejected: it would poison the cached context for other IT classes.
7. **Assertion details pinned to actual code contracts**: refresh returns only `{"accessToken"}`; logout is 204 even with garbage/absent token; branding GET is a HashMap so `appLogoUrl` may be JSON `null` (assert `jsonPath("$.appLogoUrl").value(nullValue())` pre-configuration, `"/api/files/<blobId>"` after); change-password wrong-current → 401 with message; google-settings GET never contains the secret value.

## Risks / Trade-offs

- [Shared JVM-wide containers + no cleanup → cross-class interference] → unique usernames/emails/googleSubs/states everywhere (UUID suffix); ordered methods confined to the two classes that need them; no test asserts global counts.
- [`localadmin` role/branding mutated by one IT could break another (cached context)] → role-change test promotes a *dedicated* user, never `localadmin`; branding test's admin write is the last scenario in its ordered class; defaults assertion runs first.
- [Boot-4 import trap (`AutoConfigureMockMvc` package moved)] → copy the import from the known-good `AuthFlowIT`.
- [`mvn verify` runs all IT classes including existing `AuthFlowIT`; slow (~minutes)] → authorized and expected; scoped to auth-service only.
- [A test failing on first run may indicate a real code/spec mismatch] → do not adjust the assertion to pass; report the finding (this is the ratchet's purpose).

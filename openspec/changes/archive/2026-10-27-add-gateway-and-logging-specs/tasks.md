# Tasks

## 1. Verify api-gateway spec against the implementation

- [x] 1.1 Re-confirm the routing requirement against the gateway route configuration (path predicates `/auth/**`, `/oauth2/**` + `/login/oauth2/**` → auth-service and `/api/**` → main-service, paths forwarded unchanged, no prefix stripping) and confirm the unmatched-path 404 and downstream-unreachable 5xx behaviors are the framework defaults the spec claims; verify by recording file/line evidence as a note under this task.
  - Evidence: `src/backend/api-gateway/src/main/resources/application.yml:18-49` — `/auth/**` → `${AUTH_SERVICE_URL:http://localhost:8081}` (:19-21); `/oauth2/**,/login/oauth2/**` → auth-service (:32-34); `/api/**` → `${MAIN_SERVICE_URL:http://localhost:8082}` (:39-41). No `StripPrefix` on any route (only `RemoveRequestHeader`/`RequestRateLimiter` filters), so paths forward unchanged.
  - Unmatched-path 404 and downstream-unreachable 5xx: the module has no custom error handling (its only Java file, `GatewayApplication.java`, defines just the `KeyResolver` bean) — both are Spring Cloud Gateway framework defaults, as the spec claims.
- [x] 1.2 Re-confirm the rate-limiting requirements (`/auth/**` replenish 20 / burst 40, `/api/**` replenish 30 / burst 60, one token per request, OAuth2 routes exempt, 429 on exceed is the framework default with no in-repo override) and client IP resolution (first `X-Forwarded-For` entry, else remote address); verify by recording file/line evidence as a note under this task.
  - Evidence: `application.yml:26-28` (`/auth/**` 20 / 40 / requestedTokens 1), `:46-48` (`/api/**` 30 / 60 / 1), both `key-resolver: "#{@ipKeyResolver}"` (:29, :49); the OAuth2 route (:31-36) carries only `RemoveRequestHeader=Origin` — no limiter. No status override anywhere in the file → 429 is the `RedisRateLimiter` framework default.
  - IP resolution: `GatewayApplication.java:18-28` — first `X-Forwarded-For` entry (`split(",")[0].trim()`), else remote address, else `"unknown"`.
- [x] 1.3 Re-confirm transparent proxying (gateway has no security dependency, credentials forwarded untouched), `Origin` removal on all three routes, and the cross-origin policy values (any origin pattern, credentials allowed, methods GET/POST/PUT/PATCH/DELETE/OPTIONS, any headers); verify by recording file/line evidence as a note under this task.
  - Evidence: `pom.xml:26-50` — dependencies are gateway-webflux, data-redis-reactive, actuator, devtools, test only; **no Spring Security dependency** and no filter code, so credentials pass through untouched.
  - `RemoveRequestHeader=Origin` on all three routes: `application.yml:23, :36, :43`. Cross-origin policy `:51-65`: `allowedOriginPatterns: "*"`, methods GET/POST/PUT/PATCH/DELETE/OPTIONS, `allowedHeaders: "*"`, `allowCredentials: true`.

## 2. Verify logging spec against the implementation

- [x] 2.1 Re-confirm the profile-output requirement against the logback configuration shared by all three services (`dev` = colored console, root INFO with application packages at DEBUG; `prod-render` = console JSON objects with timestamp/level/logger/thread/message; `prod-vps` = file + console; fallback = plain console INFO) and the retention requirement (daily rotation, 30-day history, 3 GB total archive cap); verify by recording file/line evidence as a note under this task.
  - Evidence: `auth-service/src/main/resources/logback-spring.xml` — colored CONSOLE (:5-9); JSON_CONSOLE with timestamp/level/logger/thread/message (:12-16); FILE `logs/application.log` (:20), daily `%d{yyyy-MM-dd}` rotation (:22), `maxHistory 30` (:23), `totalSizeCap 3GB` (:24); `dev` = root INFO + `com.telegramquiz` DEBUG (:32-37); `prod-render` = JSON_CONSOLE (:40-45); `prod-vps` = FILE + CONSOLE (:48-54); fallback = plain console INFO (:57-61).
  - Uniform across services: SHA-256 comparison — main-service and api-gateway logback files are byte-identical (`A51FBD0F…`); auth-service differs only in one XML comment ("prod-render" vs "prod"), functionally identical.
- [x] 2.2 Re-confirm the buffer requirement (capacity exactly 1000, circular overwrite of the oldest entry, entry fields timestamp/level/logger/message with no stack trace, registered under every profile); verify by recording file/line evidence as a note under this task.
  - Evidence: `auth-service/.../config/InMemoryLogAppender.java` — `MAX_ENTRIES = 1000` (:21), fixed array (:24), circular overwrite `writeIndex = (writeIndex + 1) % MAX_ENTRIES` (:44-50), `LogEntry(long timestamp, String level, String logger, String message)` with no stack-trace field (:98), attached to the ROOT logger on `ApplicationStartedEvent` regardless of profile (:82-96).
  - main-service twin confirms identical constants and record shape (`main/.../config/InMemoryLogAppender.java:21, :46, :82, :98`).
- [x] 2.3 Re-confirm the filtering and access requirements (exact level filter, case-insensitive message-or-logger match, default limit 200, oldest-first read order up to the limit, bare-array responses, `ROLE_ADMIN` guards on `GET /auth/admin/logs` and `GET /api/admin/logs`); verify by recording file/line evidence as a note under this task.
  - Evidence: `InMemoryLogAppender.getLogs` (:53-66) iterates from the oldest retained entry `(writeIndex - count + MAX_ENTRIES) % MAX_ENTRIES` and breaks at `limit`; `matches` (:68-80) = exact `level.equals` plus lowercased contains on message or logger.
  - Endpoints: `LogController.java:18` in each service (`/auth/admin/logs`, `/api/admin/logs`), `level`/`search` optional and `limit` default `"200"` (:27-29), bare `ResponseEntity<List<LogEntry>>` (:25, :30). Guards: auth `SecurityConfig.java:70`, main `SecurityConfig.java:30` — `hasAuthority("ROLE_ADMIN")`.

## 3. Documentation reconciliation

- [x] 3.1 Correct the `AGENTS.md` logging-profiles table: the `dev` row must say colored console with application packages at DEBUG while the root level stays INFO; verify the table matches the logback configuration.
  - Done: `dev` row now reads "Colored console; DEBUG for `com.telegramquiz` packages, INFO otherwise" — matches logback `:32-37`.
- [x] 3.2 Correct the `AGENTS.md` gateway description: rate limiting applies per route on `/auth/**` and `/api/**` only, with the OAuth2 sign-in routes exempt; verify the wording matches the gateway configuration and the new `api-gateway` spec.
  - Done: the Security bullet now states per-route rate limiting on `/auth/**` + `/api/**` with the OAuth2 sign-in routes exempt — matches `application.yml:24-29, :44-49` and the limiter-free OAuth2 route `:31-36`.
- [x] 3.3 Record the coverage deferral required by the ratchet rule: add an explicit note to `.agents/skills/testing-conventions/SKILL.md` that automated coverage of `api-gateway` and `logging` scenarios is deferred until a gateway/logging test-infrastructure change exists (the gateway module has no test sources today); verify the skill names the deferral.
  - Done: `testing-conventions` "Current coverage" now names the `api-gateway`/`logging` deferral and its exit condition.

## 4. Completion check

- [x] 4.1 `openspec validate --all --strict` passes with both delta specs present.
  - Done: 6/6 items pass (change + all five existing specs).

## Workflow follow-up

- Archive the change with `openspec-archive-change` after review; the deltas merge into `openspec/specs/api-gateway/` and `openspec/specs/logging/`.
- After archive, schedule the deferred test-backfill work: gateway/logging test infrastructure plus scenario tests for these two capabilities, and per-capability backfill for the existing five under the coverage-ratchet rule.

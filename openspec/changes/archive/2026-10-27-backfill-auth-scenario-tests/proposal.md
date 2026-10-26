# Proposal: backfill-auth-scenario-tests

## Why

The `auth` capability spec has 15 requirements / 36 scenarios, but the auth-service test suite covers only 7 scenarios fully and 6 partially — 23 scenarios have no automated verification at all (wrong-password sign-in, refresh-token rotation across devices, password change paths, Google registration/invitation flow, admin user management guards, branding defaults, built-in admin recovery). The coverage-ratchet rule in `openspec/config.yaml` requires named scenario tests for requirements touched by a change; `auth` is the zero-drift capability and the natural starting point for the repo-wide test backfill, so its spec scenarios can be locked down before later backfills (folders-sharing, question-bank) begin surfacing parked code bugs.

## What Changes

- Add ~5 new integration test classes (`*IT`, Testcontainers) and 2 new unit test classes (`*Test`, mock-based) to `src/backend/auth-service/src/test/java/com/telegramquiz/auth/`, covering the 23 gap scenarios and hardening the 6 partial ones.
- Tests only — no production code changes, no spec changes, no behavior changes.
- Verification runs the authorized scope: `mvn test` (fast, Docker-free) and `mvn verify` (Testcontainers ITs) in `src/backend/auth-service`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — this change is tests-only (`skip_specs: true`). The `auth` spec (`openspec/specs/auth/spec.md`) is frozen and verified correct; tests are written to assert the spec'd behavior as-is.

## Impact

- **Code:** new files under `src/backend/auth-service/src/test/java/com/telegramquiz/auth/{integration,security,config}/` only.
- **Specs:** none.
- **Docs:** none expected; if a test reveals a spec/code mismatch it will be reported rather than silently papered over (known parked bugs live in main-service, not auth-service).

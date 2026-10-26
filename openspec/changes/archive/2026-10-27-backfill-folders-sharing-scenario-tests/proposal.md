# Proposal

## Why

The folders-sharing capability has 26 scenarios but only 6 have partial test coverage; 20 scenarios have no automated verification at all. The coverage-ratchet rule in `openspec/config.yaml` requires named scenario tests for requirements touched by a change, and folders-sharing is the next capability in the test-backfill sequence. Additionally, one confirmed code bug (missing PENDING guard in `FolderService.declineInvitation()`) will surface when tests meet the code.

## What Changes

- Add integration test classes (`*IT`, Testcontainers) and unit test classes (`*Test`, mock-based) to `src/backend/main-service/src/test/java/com/telegramquiz/main/`, covering the 20 gap scenarios and hardening the 6 partial ones.
- Strengthen existing partial tests in `FolderAccessTest` and `QuestionAccessIT` to fully cover their scenarios.
- Tests only — no spec changes, no behavior changes (the bug will be reported, not fixed, unless the test forces a decision).
- Verification runs the authorized scope: `mvn test` (fast, Docker-free) and `mvn verify` (Testcontainers ITs) in `src/backend/main-service`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — this change is tests-only (`skip_specs: true`). The `folders-sharing` spec (`openspec/specs/folders-sharing/spec.md`) is frozen and will be verified against the code as-is.

## Impact

- **Code:** new files under `src/backend/main-service/src/test/java/com/telegramquiz/main/{service,repository,integration}/` only.
- **Specs:** none.
- **Docs:** none expected; if a test reveals a spec/code mismatch (the declineInvitation bug), it will be reported and a decision requested before archiving.

# Design

## Context

The folders-sharing capability has 26 scenarios: 6 have partial coverage (via `FolderAccessTest` and `QuestionAccessIT`), 20 have no tests. Main-service already has Testcontainers infrastructure (PostgreSQL), and the existing tests demonstrate the pattern: unit tests with `@ExtendWith(MockitoExtension.class)` and integration tests with `@SpringBootTest` + `@AutoConfigureMockMvc`.

One confirmed code bug exists: `FolderService.declineInvitation()` does not check if the invitation status is PENDING before declining, allowing ACCEPTED invitations to be demoted. This will surface when tests meet the code.

## Goals / Non-Goals

**Goals:**
- Write tests that cover all 26 folders-sharing scenarios
- Strengthen the 6 partially-covered scenarios to full coverage
- Follow the naming convention: `folders-sharing / <requirement> / <scenario>`
- Use existing test infrastructure (Testcontainers, MockMvc)
- Report the declineInvitation bug when tests expose it

**Non-Goals:**
- Fixing the bug unless the test forces a decision
- Changing the spec or adding new scenarios
- Implementing missing features (e.g., no new endpoints)
- Testing cross-service interactions (folders + questions visibility is out of scope for this change)

## Decisions

**1. Test class structure: split by layer, not by feature**
- **Unit tests** (`FolderServiceTest.java`): mock repositories, test `declineInvitation()` bug directly
- **Integration tests** (multiple `*IT.java` classes): real database via Testcontainers, test endpoints end-to-end
- **Rationale**: unit tests isolate the bug; integration tests verify the full flow. Mixing them in one class makes failures harder to diagnose.

**2. Test naming: follow the spec structure**
- Pattern: `folders-sharing / <requirement name> / <scenario detail>`
- Example: `folders-sharing / Accepting and declining invitations / Processing a resolved invitation`
- **Rationale**: makes it easy to trace test coverage back to the spec; matches the pattern established in auth-service.

**3. Integration test infrastructure: reuse existing setup**
- Use `@SpringBootTest` with `@AutoConfigureMockMvc` and `TestContainers`
- Do NOT use `@Transactional` rollback in integration tests (main-service pattern)
- **Rationale**: matches existing `QuestionAccessIT` pattern; rollback can hide cascade issues.

**4. Test data isolation: unique identifiers per test**
- Each test creates its own users, folders, and invitations with unique names (e.g., `user-<uuid>`, `folder-<uuid>`)
- Do not rely on test execution order
- **Rationale**: main-service tests don't use transactional rollback, so shared state can leak between tests. Unique identifiers prevent collisions.

**5. Bug surfacing strategy: write tests that match current code behavior**
- For `declineInvitation()`, write a test that documents the current behavior (e.g., "ACCEPTED invitation can be declined")
- When the test fails against the spec expectation, report the bug and pause
- **Rationale**: avoids silently adjusting tests to hide bugs; forces an explicit decision about whether to fix the code or adjust the spec.

**6. Partial coverage strengthening: add assertions to existing tests**
- `FolderAccessTest`: add missing assertions for scenarios that are partially covered
- `QuestionAccessIT`: add missing assertions for shared-folder question access
- **Rationale**: these tests already exist; strengthening them is cheaper than writing new ones.

## Risks / Trade-offs

**[Risk] Bug surfaces during testing** → **Mitigation**: pause and report; do not silently adjust the test to match buggy behavior. The user will decide whether to fix the code or update the spec.

**[Risk] Shared test state causes flaky tests** → **Mitigation**: use unique identifiers (UUIDs) for all test data; avoid assumptions about global state.

**[Risk] Complex scenarios require many setup steps** → **Mitigation**: extract helper methods (e.g., `createFolderWithOwner()`, `inviteAndAccept()`) to keep tests readable.

**[Risk] Integration tests are slow (Testcontainers startup)** → **Mitigation**: reuse the same Testcontainers instance across all IT classes (Spring caches the context); this is already the pattern in main-service.

**[Trade-off] Unit tests vs integration tests for the bug** → I chose both: unit tests isolate the bug for fast feedback, integration tests verify the fix works end-to-end. Alternative: integration tests only. Rejected because unit tests are faster and more precise for this specific bug.

# Design

## Context

The question-bank capability has 29 scenarios across 14 requirements, but only 5 scenarios currently have test coverage (17%). The existing tests in `QuestionAccessIT.java` cover question visibility and content blocks. This design outlines how to add comprehensive test coverage for the remaining 24 scenarios without modifying production code or specs.

## Goals / Non-Goals

**Goals:**
- Add tests for all 24 untested question-bank scenarios
- Organize tests logically by requirement area for maintainability
- Reuse existing test infrastructure (JUnit 5, Spring Boot Test, Testcontainers)
- Follow established test patterns from folders-sharing backfill
- Ensure tests clearly document the behavior they verify

**Non-Goals:**
- No production code changes
- No spec changes (skip_specs: true)
- Not testing Telegram bot network interactions (deferred per testing-conventions)
- Not testing UI/frontend behavior

## Decisions

### 1. Test Organization Strategy

**Decision:** Group tests into integration test classes by requirement area, following the pattern established in folders-sharing backfill.

**Rationale:** 
- Logical grouping makes tests easier to find and maintain
- Aligns with the spec structure
- Allows focused test execution when needed

**Test classes to create:**
- `QuestionVisibilityIT.java` - extend existing visibility tests
- `QuestionContentIT.java` - content blocks, hints, explanations
- `QuestionScoringIT.java` - multiple-choice, photo questions, scored totals
- `QuestionConfigurationIT.java` - briefings, after-answer settings
- `QuestionPermissionsIT.java` - editing, deletion, folder assignment
- `QuestionOrderingIT.java` - ordering and reordering
- `QuestionImportExportIT.java` - import and export functionality
- `QuestionImageIT.java` - image storage and serving

**Alternative considered:** Single large test class - rejected because it would be harder to navigate and maintain.

### 2. Test Infrastructure Reuse

**Decision:** Use existing `FolderIntegrationTestBase` pattern and Testcontainers setup.

**Rationale:**
- Consistent with folders-sharing tests
- Testcontainers already configured for PostgreSQL
- `asUser()` helper already handles authentication
- Reduces duplication and maintenance burden

**Alternative considered:** Create separate base class for question tests - rejected because FolderIntegrationTestBase already provides needed infrastructure.

### 3. Test Data Strategy

**Decision:** Use `@BeforeEach` setup methods to create test data, with `@Transactional` for automatic rollback.

**Rationale:**
- Each test starts with clean, predictable state
- Transactional rollback ensures test isolation
- Follows existing pattern from QuestionAccessIT

**Alternative considered:** Shared test data across tests - rejected because it creates test interdependencies and makes failures harder to debug.

### 4. Coverage of Existing Tests

**Decision:** Keep existing `QuestionAccessIT.java` tests and add new test classes rather than consolidating.

**Rationale:**
- Existing tests are passing and provide value
- Consolidation risks breaking working tests
- New classes can be more focused and specific

## Risks / Trade-offs

### Risk: Test Execution Time

**Risk:** Adding many integration tests increases overall test execution time.

**Mitigation:**
- Group related tests to share setup where appropriate
- Use `@Transactional` for fast rollback instead of manual cleanup
- Tests can be run selectively using `-Dtest=ClassName`

### Risk: Complex Test Setup

**Risk:** Some scenarios (import/export with images, photo questions) require complex setup.

**Mitigation:**
- Create test data builders for common entities
- Use helper methods for repetitive setup
- Document setup requirements in test comments

### Risk: Test Flakiness

**Risk:** Integration tests may be flaky due to timing or external dependencies.

**Mitigation:**
- Use Testcontainers for consistent database state
- Avoid time-dependent assertions
- No external service dependencies (Telegram bot deferred)

### Trade-off: Test Granularity

**Trade-off:** More granular tests (one per scenario) vs. fewer comprehensive tests.

**Decision:** Prefer granular tests for clarity and easier debugging.

**Rationale:**
- Each test has a single responsibility
- Failures are easier to diagnose
- Tests serve as living documentation

## Migration Plan

Not applicable - this is a test-only change with no production code modifications.

## Open Questions

None - all decisions can be made based on existing patterns and infrastructure.

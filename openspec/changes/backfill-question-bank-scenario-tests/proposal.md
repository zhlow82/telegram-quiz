# Proposal

## Why

The question-bank capability has 14 requirements with 29 scenarios, but only 5 scenarios have test coverage (17%). The remaining 24 scenarios need automated tests to ensure the implementation matches the specification and to catch regressions. This backfill follows the same pattern as the auth and folders-sharing backfills, completing the test coverage for a core capability before moving to other areas.

## What Changes

- Add integration test classes covering all 24 untested question-bank scenarios
- Tests will verify question visibility, content blocks, question kinds, scoring, photo questions, briefings, hints, explanations, editing permissions, folder assignment, ordering, import, export, and image storage
- No production code changes expected
- No spec changes (skip_specs: true)

## Capabilities

### New Capabilities

None

### Modified Capabilities

None

## Impact

- **Test files**: New test classes in `src/backend/main-service/src/test/java/com/telegramquiz/main/`
- **No API changes**: Tests only verify existing behavior
- **No database changes**: Tests use existing schema
- **No dependencies added**: Uses existing test infrastructure (JUnit 5, Spring Boot Test, Testcontainers)

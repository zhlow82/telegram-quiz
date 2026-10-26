# logging Specification

## Purpose

Defines how the backend services emit logs per deployment profile, how file logs are rotated and retained, how many recent entries each application service keeps in memory, how those entries can be filtered, and who may read them.

## Requirements

### Requirement: Profile-based log output
Each backend service SHALL select its log output from the active deployment profile: `dev` writes colored human-readable console output with application packages at debug level and everything else at info; `prod-render` writes one JSON object per entry to the console carrying timestamp, level, logger, thread, and message; `prod-vps` writes to both console and a rolling log file; with no profile active, output is plain console at info level.

#### Scenario: Development profile
- **WHEN** a service runs with the `dev` profile
- **THEN** logs appear on the console in colored human-readable form and application-package entries include debug-level events

#### Scenario: Render profile
- **WHEN** a service runs with the `prod-render` profile
- **THEN** each log entry is emitted to the console as a JSON object with timestamp, level, logger, thread, and message

#### Scenario: VPS profile
- **WHEN** a service runs with the `prod-vps` profile
- **THEN** entries are written both to the console and to a rolling log file

#### Scenario: No profile
- **WHEN** a service runs without a logging profile
- **THEN** entries are written to the console in plain form at info level

### Requirement: File log rotation and retention
Under the file-logging profile the service SHALL rotate its log file daily and SHALL retain rotated files for 30 days within a total archive cap of 3 GB, discarding the oldest archives when either limit is exceeded.

#### Scenario: Daily rotation
- **WHEN** a day boundary passes under the `prod-vps` profile
- **THEN** logging continues in a new file and the previous day's file is kept as an archive

#### Scenario: Retention exceeded
- **WHEN** rotated archives are older than 30 days or exceed 3 GB in total
- **THEN** the oldest archives are discarded

### Requirement: In-memory recent-entry buffer
The auth service and the main service SHALL each retain the most recent 1000 log entries in memory under every profile, overwriting the oldest entry once capacity is reached. Each buffered entry SHALL record the timestamp, level, logger name, and rendered message; stack traces SHALL NOT be part of a buffered entry.

#### Scenario: Buffer overflow
- **WHEN** a service logs its 1001st entry
- **THEN** the oldest buffered entry is discarded and the buffer still holds 1000 entries

#### Scenario: Entry content
- **WHEN** a warning with an exception is logged
- **THEN** the buffered entry records the timestamp, level, logger, and message without the stack trace

### Requirement: Log entry filtering
Reading the buffered entries SHALL support an exact level filter, a case-insensitive text match against the message or the logger name, and a maximum count that defaults to 200. Matching SHALL start from the oldest retained entry and stop once the maximum count is reached.

#### Scenario: Level filter
- **WHEN** an administrator requests entries with level `ERROR`
- **THEN** only error entries are returned

#### Scenario: Text search
- **WHEN** an administrator searches for a word appearing in some entries
- **THEN** entries whose message or logger contains the word are returned, matched case-insensitively

#### Scenario: Count limit
- **WHEN** more entries match than the requested maximum
- **THEN** the oldest matching entries up to the maximum are returned

### Requirement: Administrator-only log access
The auth service SHALL expose its buffered log entries at an administrator-only logs endpoint under `/auth/admin`, and the main service at an administrator-only logs endpoint under `/api/admin`, each returning the filtered entries as a list; callers without the administrator role SHALL be rejected.

#### Scenario: Administrator reads logs
- **WHEN** an administrator requests the logs endpoint
- **THEN** the response is a list of buffered entries with their timestamps, levels, loggers, and messages

#### Scenario: Member reads logs
- **WHEN** a signed-in non-administrator requests the logs endpoint
- **THEN** the request is rejected with 403

#### Scenario: Anonymous read
- **WHEN** an unauthenticated client requests the logs endpoint
- **THEN** the request is rejected with 401 or 403

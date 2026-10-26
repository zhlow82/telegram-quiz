# quiz-authoring Specification

## Purpose

Defines how a quiz is created and configured: ownership, bot credentials, timing and pass rules, ordered question selection, the draft/active/stopped lifecycle, bot token validation, and the single-active-quiz-per-token rule that prevents one Telegram bot from powering two quizzes.

## Requirements

### Requirement: Quiz ownership
The system SHALL scope every quiz read and write to the user who created it. A quiz owned by someone else SHALL behave as if it does not exist.

#### Scenario: Reading another user's quiz
- **WHEN** a user requests a quiz created by another user
- **THEN** the system responds 404

#### Scenario: Listing quizzes
- **WHEN** a user lists quizzes
- **THEN** only quizzes they created are returned, newest first

### Requirement: Quiz configuration
A quiz SHALL have a name, bot token, optional bot username, a per-question time limit, an optional total time limit, a pass score percentage, and an ordered list of questions. The per-question limit SHALL be 0-300 seconds, the total limit 0 or more seconds, and the pass score 0-100 percent; 0 SHALL disable the corresponding limit.

#### Scenario: Saving valid configuration
- **WHEN** an owner saves a quiz with a per-question limit of 30, no total limit, and a pass score of 70
- **THEN** the configuration is stored and returned

#### Scenario: Invalid time limit
- **WHEN** a per-question limit above 300 seconds is submitted
- **THEN** the system responds 400

### Requirement: Bot token secrecy
The system SHALL store bot tokens encrypted at rest and SHALL return only a masked form showing the last four characters. Updating a quiz with the masked value SHALL preserve the stored token.

#### Scenario: Reading a quiz
- **WHEN** an owner reads a quiz
- **THEN** the response contains a masked token, never the plaintext token

#### Scenario: Saving the masked token back
- **WHEN** an owner updates a quiz while submitting the masked token unchanged
- **THEN** the previously stored token remains in effect

#### Scenario: Replacing the token
- **WHEN** an owner submits a new plaintext token
- **THEN** the stored token is replaced

### Requirement: Question selection and order
A quiz SHALL include only questions accessible to the owner, in the order submitted. The quiz summary SHALL report the number of questions that count toward the score, excluding briefings and text-input questions.

#### Scenario: Selecting questions
- **WHEN** an owner saves a quiz with a list of question identifiers
- **THEN** the quiz stores those questions in the submitted order

#### Scenario: Inaccessible question
- **WHEN** the submitted list contains a question the owner cannot access
- **THEN** the system responds 404 and saves no quiz

#### Scenario: Scored count
- **WHEN** a quiz includes two scored questions, one briefing, and one text-input question
- **THEN** its summary reports a scored total of two

### Requirement: Quiz lifecycle
A new quiz SHALL start in `DRAFT`. Activating it SHALL set `ACTIVE` and start its Telegram bot; stopping it SHALL set `STOPPED` and stop the bot; deleting it SHALL stop the bot when active and remove the quiz.

#### Scenario: Activating a quiz
- **WHEN** an owner activates a draft quiz
- **THEN** the quiz becomes `ACTIVE` and its bot starts polling

#### Scenario: Stopping a quiz
- **WHEN** an owner stops an active quiz
- **THEN** the quiz becomes `STOPPED` and its bot stops responding

#### Scenario: Deleting an active quiz
- **WHEN** an owner deletes an active quiz
- **THEN** the bot is stopped and the quiz is removed

### Requirement: One active quiz per bot token
The system SHALL allow at most one active quiz per bot token. Activating a second quiz with a token already used by an active quiz SHALL fail with 409 and SHALL name the conflicting quiz.

#### Scenario: Token already active elsewhere
- **WHEN** an owner activates a quiz whose token powers another active quiz
- **THEN** the system responds 409, names the other quiz, and neither quiz changes state

#### Scenario: Activating the same quiz again
- **WHEN** an already active quiz is activated again
- **THEN** its existing bot session is replaced and the quiz stays active

### Requirement: Bot token validation
The system SHALL validate a submitted token by asking Telegram for the bot identity. The response SHALL report whether the token is valid, the bot's name and username, and whether an active quiz already uses the token.

#### Scenario: Valid unused token
- **WHEN** an authenticated user validates a token that Telegram accepts and no active quiz uses
- **THEN** the response reports the token as valid and not in use

#### Scenario: Token already in use
- **WHEN** the token belongs to another active quiz
- **THEN** the response reports the token as in use and names that quiz

#### Scenario: Telegram rejects the token
- **WHEN** Telegram rejects the token or cannot be reached
- **THEN** the response reports the token as invalid without an error status

### Requirement: Startup recovery of active quizzes
On startup the system SHALL restart the bot for every quiz marked `ACTIVE`. A failure for one quiz SHALL be logged and SHALL NOT prevent other quizzes from starting.

#### Scenario: Service restart
- **WHEN** the service restarts while two quizzes are active
- **THEN** both bots resume polling

#### Scenario: One token fails
- **WHEN** one active quiz has an unusable token
- **THEN** the other active quizzes still start and the failure is logged

### Requirement: Live bot uses the configuration captured at activation
The system SHALL feed the running bot from the quiz configuration captured when it was activated. Configuration changes saved while the quiz is active SHALL take effect only after the quiz is stopped and activated again.

#### Scenario: Editing an active quiz
- **WHEN** an owner changes questions or timings while the quiz is active
- **THEN** the running bot continues serving the previously captured configuration until reactivation

### Requirement: Session data is owner-only
The system SHALL expose a quiz's sessions, answers, and player photos only to the quiz owner. Session identifiers belonging to another user's quiz SHALL be treated as not found.

#### Scenario: Reading sessions
- **WHEN** the owner requests the sessions of their quiz
- **THEN** the sessions are returned newest first

#### Scenario: Another user reads session answers
- **WHEN** a non-owner requests answers for a session of someone else's quiz
- **THEN** the system responds 404

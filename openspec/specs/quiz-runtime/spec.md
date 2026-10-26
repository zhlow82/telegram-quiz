# quiz-runtime Specification

## Purpose

Defines what happens inside a running quiz: how a Telegram chat starts and restarts a session, how questions, timers, hints, and answers are delivered, how scoring and completion work, and how session records are persisted, retrieved, and eventually abandoned.

## Requirements

### Requirement: Starting a session
The system SHALL start a quiz session when a player sends `/start` to an active quiz's bot. Starting again in the same chat SHALL abandon the previous in-progress session for that player and begin a new one.

#### Scenario: First start
- **WHEN** a player sends `/start`
- **THEN** a new in-progress session is recorded and the first question is delivered

#### Scenario: Restarting
- **WHEN** the player sends `/start` while a session is in progress
- **THEN** the previous session is marked abandoned and a fresh session begins from the first question

#### Scenario: Quiz without questions
- **WHEN** `/start` is sent to a quiz with no questions
- **THEN** the bot replies that the quiz has no questions and starts no session

### Requirement: Welcome summary
At session start the bot SHALL report the number of scored questions, the per-question time limit when enabled, and the pass score.

#### Scenario: Quiz with a timer
- **WHEN** a session starts on a quiz with a 30-second per-question limit and a 70% pass score
- **THEN** the welcome message states the scored total, 30 seconds per question, and 70%

### Requirement: Per-chat session state
The system SHALL keep one live quiz state per Telegram chat, shared by everyone in a group chat. The player identity recorded for the session SHALL be the sender of the `/start` message.

#### Scenario: Group play
- **WHEN** several people answer in the same group chat
- **THEN** they advance the same chat state and share one session record

#### Scenario: Identity capture
- **WHEN** a player starts the quiz in a group
- **THEN** the session stores that player's Telegram id, username, and first name

### Requirement: Question delivery
The bot SHALL send a question's image blocks before its text blocks. Scored questions SHALL be labelled with their position and the scored total, and their options SHALL be offered as inline buttons. A hint button SHALL appear when the question has hints.

#### Scenario: Delivering a scored question
- **WHEN** the next question is scored and has three options
- **THEN** the bot sends the question content and three answer buttons

#### Scenario: Delivering images
- **WHEN** a question starts with image blocks
- **THEN** those images arrive before the question text

#### Scenario: Hint button presence
- **WHEN** a scored question has no hint blocks
- **THEN** no hint button is shown

### Requirement: Answer capture
The system SHALL record every answer with the question, the selected answer when one exists, whether it was correct, any photo reference, the response time, and the answer time. Correct multiple-choice answers and received photos SHALL increase the score by one.

#### Scenario: Scored answer recorded
- **WHEN** a player answers a scored question
- **THEN** an answer record is stored and the session's current question position advances

#### Scenario: Text-input answer
- **WHEN** a player submits text for a text-input question
- **THEN** the text is stored as the session's team name and no score is added

### Requirement: Stale interactions are ignored
The bot SHALL ignore answer buttons attached to a previous question and SHALL ignore text messages that arrive while no text input is expected.

#### Scenario: Old answer button
- **WHEN** a player presses an answer button from an earlier question's message
- **THEN** the press has no effect on the current question

#### Scenario: Unprompted text
- **WHEN** a player sends text while the current question is not a text-input question
- **THEN** the bot does not treat it as an answer

### Requirement: Per-question time limit
When the per-question limit is enabled, the system SHALL advance when it expires: the current question is recorded as an incorrect answer without a selection, the answer buttons are removed, and the player is told that time is up.

#### Scenario: Timing out
- **WHEN** the per-question timer expires before the player answers
- **THEN** an incorrect answer is recorded and the next question is delivered

#### Scenario: Timer disabled
- **WHEN** the per-question limit is 0
- **THEN** the question waits indefinitely for an answer

### Requirement: Total time limit
When the total limit is enabled, the system SHALL end the quiz when it expires. If a question is in progress at that moment, it SHALL be recorded as an incorrect answer before the result is shown.

#### Scenario: Deadline reached mid-question
- **WHEN** the total limit expires while the player is answering
- **THEN** that question is recorded as incorrect and the quiz completes

#### Scenario: Total limit disabled
- **WHEN** the total limit is 0
- **THEN** the quiz runs until all questions are answered

### Requirement: Completion and scoring
On completion the system SHALL compute the score as a whole-number percentage of the scored total and mark the session passed when the percentage reaches the configured pass score. It SHALL store the result, the duration from the first scored question to completion, and the number of hints used.

#### Scenario: Passing
- **WHEN** a player answers 7 of 10 scored questions correctly on a quiz with a 70% pass score
- **THEN** the complete message reports 70%, marks the result as passed, and the session is stored as completed

#### Scenario: Failing
- **WHEN** the percentage is below the pass score
- **THEN** the result is marked failed and the player is invited to start again

#### Scenario: Duration
- **WHEN** a session completes
- **THEN** the stored duration excludes the welcome and any pre-question briefing time

### Requirement: Hint accounting
The system SHALL count at most one hint per question, regardless of how many times the hint button is pressed for that question.

#### Scenario: Pressing hint twice
- **WHEN** a player presses the hint button twice on the same question
- **THEN** the hint is shown twice and the session records one hint for that question

### Requirement: Explanation and navigation after an answer
After evaluating an answer the bot SHALL show the result and any explanation. When the question shows a next-question button the bot SHALL wait for that button; otherwise it SHALL advance automatically after a short delay.

#### Scenario: Waiting for the next button
- **WHEN** an answered question shows its next-question button and has an explanation
- **THEN** the quiz stays on that question until the player presses the button

#### Scenario: Automatic advance
- **WHEN** an answered question hides its next-question button
- **THEN** the next question follows automatically after a short delay

#### Scenario: Briefing navigation
- **WHEN** a briefing's primary button is pressed
- **THEN** the briefing keyboard is removed and the next question is delivered

#### Scenario: Briefing secondary button
- **WHEN** a briefing's secondary button is pressed
- **THEN** the briefing keyboard is removed and the next question follows after a one-second delay

### Requirement: Session persistence and retrieval
The system SHALL persist sessions with an in-progress, completed, or abandoned status and SHALL persist each answer individually. Session answers SHALL be returned in the order they were answered, and sessions SHALL be listed newest first.

#### Scenario: Reviewing a session
- **WHEN** the quiz owner requests the answers of a completed session
- **THEN** the answers are returned chronologically with correctness and response times

#### Scenario: Player photos
- **WHEN** the owner requests a player photo through the session endpoint
- **THEN** the image is fetched from Telegram and returned

### Requirement: Abandoned session cleanup
The system SHALL mark an in-progress session abandoned when it has had no activity for 6 hours. The check SHALL run hourly.

#### Scenario: Stale session
- **WHEN** a session has been inactive for more than 6 hours
- **THEN** the next cleanup marks it abandoned and records the abandonment time

#### Scenario: Recently active session
- **WHEN** a session had activity within the last 6 hours
- **THEN** cleanup leaves it in progress

### Requirement: Bot availability follows quiz state
A quiz's bot SHALL respond to players only while the quiz is active. Stopping the quiz or shutting down the service SHALL cancel pending timers and end live chat states.

#### Scenario: Stopped quiz
- **WHEN** the owner stops an active quiz
- **THEN** the bot stops responding to new updates

#### Scenario: Service shutdown
- **WHEN** the service shuts down
- **THEN** every active bot is stopped and its in-memory chat state and timers are cleared

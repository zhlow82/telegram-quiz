---
name: bot-conventions
description: Use when creating or modifying the Telegram bot runtime in main-service. Covers bot lifecycle management, the activation-time configuration snapshot, per-chat state, timer scheduling, callback handling, answer rules, message formatting, and Telegram file/token handling.
---

# Bot Conventions

> Scope: implementation patterns only. Behavior contracts live in `openspec/specs/quiz-runtime/` and `openspec/specs/quiz-authoring/`; the propose → apply → archive workflow is in `AGENTS.md`.

## Architecture Overview

```
TelegramBotManager (@Service)        registry + lifecycle, one long-polling bot per ACTIVE quiz
  └─ QuizBotSession                  extends TelegramLongPollingBot; plain object, NOT a Spring bean
       ├─ QuizBotData (record)       immutable activation snapshot — the bot's only config source
       ├─ ChatQuizState              mutable per-chat state (volatile fields), keyed by chatId
       └─ QuizSessionService         the bot's ONLY database write path
TelegramFileService (@Service)       downloads player photos via the Telegram API
BotTokenEncryptionService (@Service) AES-256-GCM encryption of bot tokens at rest
```

Long polling only (`TelegramLongPollingBot`, registered via `TelegramBotsApi(DefaultBotSession.class)`); no webhooks, no inbound HTTP surface for bots. Dependency: `org.telegram:telegrambots:6.9.7.1`.

## Bot Lifecycle (TelegramBotManager)

Three concurrent maps: `activeSessions` (quizId → telegrambots `BotSession`), `activeBots` (quizId → `QuizBotSession`), `tokenToQuizId` (raw token → quizId).

```java
public void startBot(QuizBotData data) {
    stopBot(data.quizId());                       // idempotent restart: stop any existing session first
    Long existing = tokenToQuizId.get(data.botToken());
    if (existing != null && !existing.equals(data.quizId())) {
        throw new IllegalStateException("Bot token is already in use by quiz " + existing); // → 409
    }
    // register via botsApi, then populate activeSessions / activeBots / tokenToQuizId
}
```

- `stopBot(quizId)`: removes from both maps, cleans `tokenToQuizId` entries by value, `session.stop()`, `bot.shutdown()`. Safe to call for a quiz that is not running.
- Startup recovery: `@EventListener(ApplicationReadyEvent.class)` iterates `findAllByStatus(ACTIVE)` with a **per-quiz try/catch** — one unusable token must never block the other quizzes.
- `@PreDestroy shutdownAll()`: iterate a snapshot copy of the keys and `stopBot` each.

**Hard invariant — one active quiz per bot token.** Two guards, never weaken either: the DB-level guard in `QuizService.activate()` (`findTokenConflict` → `ResponseStatusException(CONFLICT)` naming the other quiz) and the runtime `tokenToQuizId` guard above (covers app-startup restarts). `IllegalStateException` maps to 409 via `GlobalExceptionHandler`.

## Activation Snapshot

`QuizService.buildBotData(quiz)` decrypts the token, sorts questions by `orderIndex`, maps each to `QuizBotQuestion` with null-safe `List.of()` fallbacks, and copies timings + pass score into `QuizBotData` (both immutable records in the `bot` package).

- The running bot reads **only** this snapshot; edits made while a quiz is `ACTIVE` take effect after stop + re-activate.
- **Never add repository or JPA entity dependencies to `QuizBotSession`.** Image bytes come from `ImageBlobService`; persistence goes through `QuizSessionService`. That separation is what makes the snapshot rule enforceable.

## Per-Chat State (ChatQuizState)

`ConcurrentHashMap<Long chatId, ChatQuizState>` — one live state per Telegram chat, shared by everyone in a group. Fields are `volatile` (mutated from the long-polling thread and scheduler threads). Key fields:

| Field | Purpose |
|---|---|
| `questionIndex` | cursor into `quizData.questions()` (includes briefing/text-input items) |
| `scoredQuestionNumber` / `totalQuestions` | display counter `Q n/total` (total = scored count only) |
| `score` | correct MCQ answers + received photos |
| `lastMessageId` | id of the active keyboard message → stale-callback guard |
| `waitingForTextInput` | arms text handling for team-input questions |
| `questionStartedAt` / `activeStartAt` | response time per question / duration basis (first **scored** question) |
| `hintCount` + `lastHintedQuestionIndex` | hints counted at most once per question |
| `photoAnsweredForQuestion` | photo replace-without-double-point guard |
| `deadline`, `timeoutFuture`, `deadlineFuture` | total-time deadline + timer handles (`cancelTimeout()` / `cancelDeadline()`) |

## Update Dispatch and Callbacks

Wrap the whole `onUpdateReceived` body in try/catch — a failed update must never kill long polling. Branches: `/start` → `startOrRestartSession`; text → `handleTextMessage`; photo → `handlePhotoMessage`; callback query → `handleCallback` (always answer `AnswerCallbackQuery` first).

Callback data constants: `"ans:<index>"`, `"hint"`, `"briefing:ready"`, `"briefing:start-timer"`, `"after-answer:ready"`.

**Stale-message guard:** every keyboard-bearing send captures the sent message id into `state.lastMessageId` (via the `Consumer<Message>` callback of `sendMessageWithKeyboard`); callback handlers ignore presses whose message id differs. Keep this on every new interactive message.

## Timers

Per-bot `ScheduledExecutorService` (2 daemon threads, created in the constructor, `shutdownNow()` in `shutdown()`).

- Per-question timeout: armed **only** for scored non-photo questions when `timePerQuestionSeconds > 0`; on expiry records an incorrect answer with no selection, removes the keyboard, sends "Time's up", advances.
- Briefing "Start Timer" button → fixed **1-second** delay; automatic advance after an answer → **2-second** delay.
- Total deadline: armed once at session start when `totalTimeLimitSeconds > 0`; records the in-flight question incorrect, then `endQuiz`.
- **Identity guard on every scheduled task:** `if (sessions.get(chatId) != state) return;` (reference comparison) — a restarted session invalidates old tasks. Cancel timers at every transition point (restart, callback, photo answer, deadline fire, shutdown).

## Answer Rules

- Multiple choice: `correct = q.answer() != null && q.answer().trim().equalsIgnoreCase(selected.trim())` — a null stored answer is never correct.
- Photo questions: a received photo is always recorded correct (+1); a second photo for the same question updates via `updatePhotoAnswer` **without** another point (`photoAnsweredForQuestion` guard).
- Text input: stored as the session's team name, no score; `{player_input}` placeholders in later text are substituted with the input.

## Message Sending

- `parseMode("HTML")` always. Escape player/user content with `escapeHtml`; question/explanation text goes through `renderTelegramHtml` (escape everything, then re-enable the tag whitelist `b/strong`, `i/em`, `u`, `s/del`, `code`, `br`).
- **Images before text**: `sendImageBlocks` runs before any text bubble for a question, hint, or explanation. Image blocks reference `ImageBlob` ids and are read straight from the database (`ImageBlobService.findById`) — never through the public `GET /api/files/**` route.
- `sendText` catches `TelegramApiException` and logs — never rethrow into the update loop.
- Remove keyboards with `EditMessageReplyMarkup` + an empty keyboard list.

## Persistence

All bot writes go through `QuizSessionService` (`recordStarted`, `recordCompleted`, `recordTeamName`, `recordAnswer`, `updatePhotoAnswer`), each call individually try/caught on the bot side — persistence failures must never break the chat flow. The abandoned-session cleanup (`@Scheduled(fixedRate = 3600000)`, 6-hour inactivity threshold) lives in `QuizSessionService`, not in the bot.

## Tokens and Telegram Files

- `BotTokenEncryptionService`: AES/GCM/NoPadding, 12-byte random IV per call, output `Base64(IV || ciphertext)`; key from `app.bot-token-encryption-key`. Tokens are decrypted only when building a snapshot, restarting bots at startup, or calling the Telegram API in `TelegramFileService`.
- `TelegramFileService`: plain `java.net.http` client — `getFile` then `https://api.telegram.org/file/bot<token>/<path>`; content type from the file extension; 404 with `Cache-Control: no-store` when Telegram has no file.
- `BotController` `POST /api/bot/validate-token` (authenticated): calls `getMe` via `RestClient`; an invalid token or unreachable Telegram returns **HTTP 200 with `valid=false`** — never an error status — plus `inUse`/`inUseByQuizName` from `quizService.findTokenConflict(token, excludeQuizId)`.

## Testing Rules

- Never touch the Telegram network in tests: mock `TelegramBotManager` in service tests (see `QuizActivationTest` — 409-with-name, activation/stop bot calls, token masking) and unit-test `BotTokenEncryptionService` directly (`BotTokenEncryptionServiceTest`).
- Name tests after the capability and scenario: `@DisplayName("quiz-authoring / One active quiz per bot token / Conflict returns 409 and names the quiz")`.
- The full bot flow gets its own extracted engine in a later change — until then, do not write integration tests against `QuizBotSession` itself.

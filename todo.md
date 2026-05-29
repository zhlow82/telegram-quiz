# Telegram Quiz — TODO

## Phase 0 — Infrastructure & Fixes
- [x] Fix auth-service Flyway not running (replace `flyway-core` with `spring-boot-starter-flyway`)
- [x] Disable VS Code debugger attach (`noDebug: true` in launch.json)
- [ ] Fix main-service startup (not yet investigated)
- [ ] Fix api-gateway startup (not yet investigated)
- [ ] Fix frontend startup (npm run dev exits code 1)

---

## Phase 1 — Question Bank

### Backend (main-service)
- [ ] Create Flyway migration: `questions` table
  - Fields: `id`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_option`, `category`, `difficulty`, `explanation`, `created_at`, `updated_at`
- [ ] Create `Question` JPA entity
- [ ] Create `QuestionRequestDto` and `QuestionResponseDto`
- [ ] Create `QuestionRepository` (Spring Data JPA)
- [ ] Create `QuestionService` (CRUD)
- [ ] Create `QuestionController` with endpoints:
  - `GET    /api/questions` — list all (with filter by category / difficulty)
  - `GET    /api/questions/{id}` — get single
  - `POST   /api/questions` — create
  - `PUT    /api/questions/{id}` — update
  - `DELETE /api/questions/{id}` — delete
- [ ] Add `/api/questions/**` route to api-gateway

### Frontend
- [ ] Add "Question Bank" page (`/questions`)
- [ ] Question list table (category, difficulty, question text, actions)
- [ ] Filter bar (by category, by difficulty)
- [ ] Create / Edit question form (modal or dedicated page)
- [ ] Delete question with confirmation dialog
- [ ] Add "Question Bank" link to sidebar / nav

---

## Phase 2 — Quiz Setup Wizard

### Backend (main-service)
- [ ] Create Flyway migration: `quizzes` table
  - Fields: `id`, `name`, `bot_token` (encrypted AES), `time_per_question_seconds`, `pass_score_percent`, `status` (DRAFT / ACTIVE / STOPPED), `created_at`
- [ ] Create Flyway migration: `quiz_questions` join table (quiz_id, question_id, order_index)
- [ ] Create `Quiz` and `QuizQuestion` JPA entities
- [ ] Create `QuizService` (CRUD + activate/stop)
- [ ] Create `QuizController` with endpoints:
  - `POST   /api/quizzes` — create quiz (save bot token, config, selected questions)
  - `GET    /api/quizzes` — list quizzes
  - `GET    /api/quizzes/{id}` — get quiz detail
  - `PUT    /api/quizzes/{id}` — update
  - `DELETE /api/quizzes/{id}` — delete
  - `POST   /api/quizzes/{id}/activate` — start bot polling
  - `POST   /api/quizzes/{id}/stop` — stop bot polling
- [ ] `POST /api/bot/validate-token` — call Telegram `getMe` to validate a bot token before saving

### Frontend — Quiz Wizard (step-by-step)
- [ ] **Step 1** — Instructions page: how to create a bot with BotFather (`/newbot` in Telegram)
- [ ] **Step 2** — Bot token input + "Validate" button (calls `/api/bot/validate-token`, shows bot name on success)
- [ ] **Step 3** — Quiz config: name, time per question, pass score %
- [ ] **Step 4** — Question selector: pick questions from the bank (filter by category/difficulty, checkbox list)
- [ ] **Step 5** — Review & Activate summary screen
- [ ] Wizard progress indicator (step 1 of 5)
- [ ] Add "Create Quiz" button / nav link

---

## Phase 3 — Telegram Bot Runtime

### Backend (main-service)
- [ ] Add `telegrambots-longpolling` dependency to pom.xml
- [ ] Create `TelegramBotService` — manages bot lifecycle (start/stop polling per quiz)
- [ ] Create `QuizBotHandler` — processes incoming Telegram updates:
  - `/start` command → greet user, begin quiz
  - Inline keyboard callback (A/B/C/D answer) → validate, record, send next question
  - Final question → compute score, send result message
- [ ] Create Flyway migration: `quiz_sessions` table
  - Fields: `id`, `quiz_id`, `telegram_user_id`, `telegram_username`, `current_question_index`, `score`, `started_at`, `finished_at`, `status`
- [ ] Create `QuizSession` entity + repository
- [ ] On activate: start polling thread; on stop: gracefully terminate thread

### Frontend
- [ ] Quiz dashboard page — show active quiz status, connected bot name, live participant count
- [ ] Stop quiz button

---

## Phase 4 — Results & History (future)
- [ ] Results list per quiz (participant, score, pass/fail, duration)
- [ ] Export results to CSV
- [ ] Leaderboard view
- [ ] Webhook support (production mode)
- [ ] Multiple simultaneous quiz sessions

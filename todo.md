# Telegram Quiz — TODO

## Infrastructure
- [x] Fix auth-service Flyway not running (replace `flyway-core` with `spring-boot-starter-flyway`)
- [x] Fix main-service startup
- [x] Fix api-gateway startup
- [x] Fix frontend startup

---

## Phase 1 — Question Bank ✅
- [x] `questions` table — Flyway migration
- [x] `Question` JPA entity (content blocks JSONB, hint, explanation, mark, folder)
- [x] `QuestionRequestDto` / `QuestionResponseDto`
- [x] `QuestionRepository` + `QuestionService` (CRUD, reorder, folder assign)
- [x] `QuestionController` — `GET/POST/PUT/DELETE /api/questions`, `PATCH /reorder`, `PATCH /{id}/folder`
- [x] `image_blobs` table — `POST /api/files/upload`, `GET /api/files/{id}`
- [x] Question Bank UI — list, search, create/edit modal, delete, duplicate
- [x] Question types: Multiple Choice, Text Input, Photo, Briefing
- [x] Mark (points) per question
- [x] Last-modified time + user display on question rows (`updated_by` column)

---

## Phase 1b — Folders & Sharing ✅
- [x] `folders` + `folder_members` tables — Flyway migrations
- [x] Folder CRUD + drag-to-reorder
- [x] Folder member invite / role management (OWNER, CO_OWNER, CONTRIBUTOR)
- [x] Questions scoped to folders; shared-folder question access
- [x] Question Bank sidebar: owned + shared folders, drag-to-assign questions

---

## Phase 2 — Quiz Setup Wizard ✅
- [x] `quizzes` + `quiz_questions` tables — Flyway migrations
- [x] `Quiz` / `QuizQuestion` entities + DTOs
- [x] `QuizController` — full CRUD, activate, stop
- [x] `POST /api/bot/validate-token` — Telegram `getMe` validation
- [x] Quiz Wizard — 5 steps: Instructions → Bot Token → Config → Questions → Review
- [x] Timing modes: No Timer / Per Question / Total Quiz timer
- [x] Pass score (percentage)
- [x] Question selector with folder filter, drag-to-reorder, points summary
- [x] Quiz list page with activate/stop actions

---

## Phase 3 — Telegram Bot Runtime
- [x] `quiz_sessions` table — Flyway migration
- [x] `QuizSession` entity + repository
- [ ] `TelegramBotService` — manage bot lifecycle (start/stop polling per quiz)
- [ ] `QuizBotHandler` — process Telegram updates (`/start`, inline keyboard answers, scoring)
- [ ] Quiz dashboard — live participant count, session status
- [ ] Per-question timer enforcement in bot
- [ ] Total quiz timer enforcement in bot

---

## Phase 4 — Results & History
- [ ] Results list per quiz (participant, score, pass/fail, duration)
- [ ] Export results to CSV
- [ ] Leaderboard view

---

## Admin & Auth ✅
- [x] User management (list, create, edit profile, set role, activate/deactivate, delete)
- [x] Invitation codes (generate, deactivate, delete, re-activate)
- [x] Google OAuth2 — dynamic credentials stored in `app_settings`, invitation-code gating
- [x] App branding — admin can set app name, login welcome text, logo image
- [x] Profile page — update name, change password

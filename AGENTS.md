# Telegram Quiz — Opencode Instructions

## Project Overview

A full-stack Telegram quiz management platform. Users create quiz questions, configure Telegram bots, and run live quizzes where Telegram users answer questions in real-time.

## Architecture

```
src/backend/auth-service/    → JWT auth, user management, OAuth2    (port 8081)
src/backend/main-service/    → Questions, quizzes, bot runtime       (port 8082)
src/backend/api-gateway/     → Spring Cloud Gateway routing          (port 8080)
src/frontend/                → Vue 3 SPA                             (port 5173)
```

All frontend API calls go through the gateway at `localhost:8080`. Vite proxies `/auth`, `/api`, `/oauth2`, `/login/oauth2` to the gateway.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3.4, TypeScript 5.4, Pinia 2, Vue Router 4, Axios, Vite 5, Tailwind CSS 3, @lucide/vue, vue-draggable-plus |
| Backend | Spring Boot 4.0.6, Spring Security, Spring Data JPA, Spring Cloud Gateway 2025.1.1 |
| Auth | JWT (jjwt 0.12.6), BCrypt, Google OAuth2 |
| Database | PostgreSQL 17 (JPA ddl-auto: validate, schema managed by Flyway) |
| Cache | Redis 7 |
| Bot | telegrambots 6.9.7.1 (long-polling) |
| Build | Maven, Java 21 (Eclipse Temurin) |

## Where the rules live

- Always-on invariants, commands, ports, and hard "never" rules: this file (authoritative)
- Detailed domain patterns and examples: `.agents/skills/<domain>/SKILL.md`
- Behavior contracts: `openspec/specs/`
- OpenSpec artifact rules: `openspec/config.yaml`

If a skill disagrees with this file on a hard rule, this file wins — fix the skill. If a change alters an implementation pattern, update the matching skill in the same change.

## Commands

```bash
# Infrastructure (local dev — auto-started by devcontainer)
# To manage manually: docker compose -f .devcontainer/docker-compose.yml up -d

# Backend (each in separate terminal)
cd src/backend/auth-service && mvn spring-boot:run
cd src/backend/main-service && mvn spring-boot:run
cd src/backend/api-gateway && mvn spring-boot:run

# Frontend
cd src/frontend && npm install && npm run dev

# Frontend type check
cd src/frontend && npx vue-tsc --noEmit

# Frontend build
cd src/frontend && npm run build

# Full stack deployment (VPS)
docker compose up -d --build
```

## Testing

**Execution policy:** write and update test cases as part of behavior changes, but do **not** run test suites unless the user explicitly asks. Test runs are opt-in because the integration suite is slow; CI is the authoritative run.

- **Backend fast tests:** `cd src/backend/auth-service && mvn test` (same for `main-service`). No Docker required.
- **Backend integration tests:** `mvn verify` in each service. Requires Docker Desktop for Testcontainers PostgreSQL/Redis.
- **Frontend tests:** `cd src/frontend && npm run test:unit` (watch: `npm run test:unit:watch`).
- **Type check / build:** `npx vue-tsc --noEmit` and `npm run build`.
- Name tests after the capability and scenario they cover (`@DisplayName("auth / Password sign-in / Deactivated account")`).
- Use PostgreSQL containers, never H2. Do not test private methods or the Telegram network.
- When asked to run tests, run only the scope requested (single class, service, or frontend file) and report the command used.
- Full conventions: `testing-conventions` skill.

### End-user local hosting (no dev tools required)

The repo ships a zero-config distribution flow for people who just want to run the app on their own machine (only Docker Desktop needed). Platform scripts live in `installation/windows/` and `installation/mac/` subfolders:

- `windows/start.bat` — checks Docker, builds & starts the full stack, waits until the app responds at `http://localhost/tg-quiz`, then prints Ready
- `windows/stop.bat` — stops all services (data preserved via Docker volumes)
- `windows/zip.bat` / `mac/zip.sh` — creates a slim `telegram-quiz.zip` distribution package (`tar` with excludes for `node_modules`/`target`/`dist`/`.git`/`.env`) containing `src`, `docker-compose.yml`, `USAGE-GUIDE.txt` and **only its own platform's** `start`/`stop`/`SETUP-GUIDE.txt` files at the package root (the zip scripts themselves are not shipped to end users)
- `windows/SETUP-GUIDE.txt` — step-by-step Windows end-user guide (install Docker, extract zip, start, login, how to use)
- `mac/start.sh`, `mac/stop.sh`, `mac/SETUP-GUIDE.txt` — macOS equivalents (run via Terminal: `chmod +x start.sh stop.sh`, then `./start.sh`)
- `USAGE-GUIDE.txt` — platform-neutral end-user how-to (shared by both platforms)

If you change anything that affects these scripts (ports, URLs, env vars, compose file names), update the Windows AND Mac versions and re-run `zip.bat`/`zip.sh` to refresh the package.

**Important:** The user starts backend services manually via VS Code `launch.json`. If you start any server yourself for testing, **always stop it after testing is complete**.

**Git:** The user prefers to handle git commits and pushes to GitHub themselves. Do not commit or push code unless explicitly asked.

## Spec Workflow (OpenSpec)

Behavior is specified with [OpenSpec](https://openspec.dev). Two layers, no overlap:

- `AGENTS.md` + `.agents/skills/` — how to write code (conventions, patterns, commands)
- `openspec/specs/` — what the system does (observable behavior, invariants, permissions)

Rules:

- Before implementing a feature or behavior change, create a change with the `openspec-propose` skill (`/openspec-propose` in OpenCode and GitHub Copilot, `$openspec-propose` in Codex). Review `proposal.md`, `specs/`, `design.md`, and `tasks.md` before coding.
- Implement with `openspec-apply-change` (`/openspec-apply-change` in OpenCode and Copilot, `$openspec-apply-change` in Codex). `openspec/specs/` is read-only during implementation — requirement changes go in the change's `specs/` delta and merge when the change is archived with `openspec-archive-change`.
- Run `openspec validate --all --strict` before archiving. Use `openspec list` for active changes and `openspec list --specs` for the capability inventory.
- When archiving, sweep `AGENTS.md` and the affected `.agents/skills/` for statements the change invalidated (facts, counts, patterns, APIs) and fix them in the same archive — stale docs are an incomplete archive.
- Keep at most 2–3 active changes at once to limit archive conflicts within the same capability.
- Internal refactors, dependency bumps, and tooling changes need no proposal. Anything that changes observable behavior does.
- Workflow skills are generated once under `.agents/skills/` and are read by OpenCode, GitHub Copilot, and Codex. Refresh them with `openspec update`; do not re-run `openspec init --tools ...` with extra tools or the per-tool copies return.
- Commit `openspec/`, `AGENTS.md`, and `.agents/skills/` with the code.

If the CLI is missing, install it with `npm install -g @fission-ai/openspec@latest`. After upgrading, run `openspec update` to refresh the generated skills.

## Logging Profiles

| Profile | Output | Use Case |
|---|---|---|
| `dev` | Colored console; DEBUG for `com.telegramquiz` packages, INFO otherwise | Local development |
| `prod-render` | JSON structured to console | Render cloud platform |
| `prod-vps` | File + console, daily rotation (30 days, 3GB cap) | VPS with Docker Compose |

All profiles include an **in-memory circular buffer** (last 1000 entries) accessible via:
- `GET /auth/admin/logs` (auth-service)
- `GET /api/admin/logs` (main-service)

## Backend Conventions

### Package Structure

- **auth-service**: `com.telegramquiz.auth.{layer}` — layers: `model`, `dto`, `repository`, `service`, `controller`, `security`, `config`
- **main-service**: `com.telegramquiz.main.{layer}` — layers: `entity`, `dto`, `model`, `repository`, `service`, `controller`, `security`, `config`, `bot`

### Code Patterns

- **Entities**: Lombok `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`; `@Builder.Default` for collection defaults; `GenerationType.IDENTITY` for IDs.
- **DTOs**: Java `record` types with `@Valid`, `@NotBlank`, `@Size`, and `@Pattern` validation.
- **Dependency injection**: constructor injection via `@RequiredArgsConstructor` with `final` fields. Never use `@Autowired`.
- **Controllers**: `@RestController` with `@RequestMapping`; extract the username via `@AuthenticationPrincipal String username`.
- **Services**: `@Service`; manual DTO mapping via private `toDto()` methods (no MapStruct); `@Slf4j` for logging.
- **Error handling**: `ResponseStatusException` for HTTP errors and `EntityNotFoundException` for 404s. A `GlobalExceptionHandler` maps them (see Important Notes).

### Database Migrations (Flyway)

- **auth-service**: `src/backend/auth-service/src/main/resources/db/migration/` — history table `flyway_schema_history`
- **main-service**: `src/backend/main-service/src/main/resources/db/migration/` — history table `flyway_schema_history_main`
- Never edit or delete an applied migration; always add a new file with the current local timestamp.
- Never execute migrations manually — the user starts the service and Flyway applies them. Run them manually only when explicitly asked.
- `ddl-auto: validate`: Flyway owns the schema; always update the matching Java entity in the same change.
- Naming, SQL templates, and common patterns: see the `database-migrations` skill.

### Security

- Stateless JWT. Access token: 24 hours. Refresh token: 7 days (stored in Redis).
- Tokens carry a `type` claim (`access` | `refresh`). `JwtAuthFilter` in **both** services only authenticates `type=access` — refresh tokens presented as Bearer tokens are rejected.
- Both services sign/validate JWTs independently with the same `JWT_SECRET`. The secret falls back to a built-in dev key when the env var is unset; override `JWT_SECRET` for any non-local deployment.
- Roles: `ROLE_ADMIN`, `ROLE_MEMBER`.
- `localadmin` / `password88` (ROLE_ADMIN) is seeded by `DataInitializer` in **every** environment by design (documented in `installation/windows/SETUP-GUIDE.txt` and `installation/mac/SETUP-GUIDE.txt`).
- Bot tokens are AES-256-GCM encrypted at rest (`BOT_TOKEN_ENCRYPTION_KEY`, built-in dev fallback).
- **One bot token can only power one active quiz at a time.** `QuizService.activate()` rejects a second active quiz with the same token (409 + quiz name); `TelegramBotManager` keeps a `tokenToQuizId` map as a second guard (covers app-startup restarts); `POST /api/bot/validate-token` reports `inUse`/`inUseByQuizName` so the wizard can warn before saving.
- main-service: `/api/admin/**` requires `ROLE_ADMIN` (e.g. `GET /api/admin/logs`). Session answers/photos endpoints (`/api/quizzes/sessions/**`) verify the caller owns the quiz.
- Google OAuth2 credentials stored in `app_settings` table (dynamic registration).
- The gateway is a transparent proxy (rate-limited per IP on `/auth/**` and `/api/**`, OAuth2 sign-in routes exempt; `Origin` header removed); all authorization happens downstream in auth-service and main-service.

## Frontend Conventions

### Code Patterns

- **Components**: `<script setup lang="ts">` (Composition API). Template first, script second, style third.
- **Naming**: PascalCase for components, camelCase for services/stores/types/refs/functions.
- **Services**: plain objects with arrow-function methods (e.g., `quizService.list()`); never call axios directly from a component.
- **Types**: PascalCase interfaces co-located in `src/types/`.
- **Store**: Composition API pattern `defineStore('name', () => { ... })`.
- **Props/Emits**: `defineProps<{...}>()` and `defineEmits<{...}>()` with generic type parameters.
- **Path alias**: `@/` maps to `./src/`; use it for all imports.
- **Styling**: Tailwind inline; `slate-*` neutrals, `primary` (sky-500) primary actions, red/green/amber semantics. Primary CTAs use `bg-primary hover:bg-primary-hover rounded-full`; sidebar active items are `bg-primary text-white rounded-full` on `bg-white`.
- **Modals**: `<teleport to="body">` for z-index isolation. **NEVER** add a click handler to the backdrop (overlay); users dismiss via X or Cancel.
- **Layout**: all authenticated views wrap in `<AppLayout>`.
- **Drag-and-drop**: `vue-draggable-plus` for list reordering, native HTML5 DnD for cross-container moves.

### Router

- Base path `/` in local dev, configurable via `VITE_BASE_URL`.
- Route meta: `requiresAuth`, `requiresGuest`, `requiresAdmin`; guards redirect to `/login` or `/home`.

### API Client

- 401 handling is centralized in `src/services/api.ts` (single-flight refresh via `/auth/refresh`, then retry; on failure clear session and redirect to login). Use the service layer, don't reimplement it.
- Vite dev proxy: `/auth`, `/api`, `/oauth2`, `/login/oauth2` → `http://localhost:8080`.

For full examples, use the `frontend-conventions` skill.

## Important Notes

- Both services have a `@RestControllerAdvice` (`GlobalExceptionHandler`) mapping `EntityNotFoundException` → 404, `ResponseStatusException`, validation failures → 400, and a generic 500 fallback.
- CORS: backend services hardcode `http://localhost:5173`; the gateway uses `allowedOriginPatterns: "*"` with credentials (mostly inert — dev uses Vite proxy, prod uses Nginx same-origin).
- `GET /api/files/**` is publicly accessible without auth (image serving); uploads (`POST /api/files/upload`) require auth.

## Deployment

Two deployment methods are supported:

### VPS (Docker Compose)
- `docker-compose.yml` — full stack deployment with all services containerized; compose project name: `telegram-quiz`
- Each service has its own `Dockerfile` (backend uses multi-stage Maven build, frontend uses Node build + Nginx)
- Environment variables use defaults in `docker-compose.yml` and `application.yml` (`JWT_SECRET` / `BOT_TOKEN_ENCRYPTION_KEY` fall back to built-in dev keys; override for any non-local deployment)
- `FRONTEND_URL` controls the OAuth2 post-login redirect target (default `http://localhost/tg-quiz`; set to the public origin on a real server)
- Frontend Nginx proxies `/auth`, `/api`, `/oauth2`, `/login/oauth2` to the API gateway
- The gateway routes `/auth/**`, `/api/**`, `/oauth2/**`, `/login/oauth2/**` and needs `REDIS_HOST`/`REDIS_PORT` for rate limiting (set in compose)
- Database and Redis use Docker volumes for persistence
- Log files stored in Docker volumes (`auth-logs`, `main-logs`, `gateway-logs`)

### Render (Cloud)
- `render.yaml` — Render blueprint for one-click deployments; the devcontainer stack uses project name `telegram-quiz-dev`
- Defines: 4 web services, 1 managed PostgreSQL, 1 managed Redis
- Database credentials auto-injected via `fromDatabase` (individual properties: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- Redis connection auto-injected via `fromService`
- JWT secret shared between auth and main services via direct `generateValue: true` in each service
- `FRONTEND_URL` must be set manually in the Render dashboard to your frontend service's public URL (blueprint ships with a `YOUR-FRONTEND.onrender.com` placeholder)
- Frontend `API_GATEWAY_URL` injected at runtime via `envsubst` in Nginx config

## Deployment Config Maintenance

Whenever adding a new library, dependency, service, port, environment variable, or infrastructure setup that affects how the application runs:
- **Always review and update `docker-compose.yml`** if the change requires new containers, volumes, networks, ports, or environment variables
- **Always review and update `render.yaml`** if the change requires new services, env vars, build commands, or resource allocations
- **Always review and update any relevant `Dockerfile`** if the change affects how a service is built or run
- **Always update `docker-compose.yml`** if new environment variables are introduced (edit the defaults directly)

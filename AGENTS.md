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

### End-user local hosting (no dev tools required)

The repo ships a zero-config distribution flow for people who just want to run the app on their own machine (only Docker Desktop needed):

- `start.bat` — checks Docker, builds & starts the full stack, waits until the app responds at `http://localhost/tg-quiz`, then prints Ready
- `stop.bat` — stops all services (data preserved via Docker volumes)
- `zip.bat` — creates a slim `telegram-quiz.zip` distribution package (`tar` with excludes for `node_modules`/`target`/`dist`/`.git`/`.env`)
- `SETUP-GUIDE.txt` — step-by-step end-user guide (install Docker, extract zip, start, login, how to use)

If you change anything that affects these scripts (ports, URLs, env vars, compose file names), update them and re-run `zip.bat` to refresh the package.

**Important:** The user starts backend services manually via VS Code `launch.json`. If you start any server yourself for testing, **always stop it after testing is complete**.

**Git:** The user prefers to handle git commits and pushes to GitHub themselves. Do not commit or push code unless explicitly asked.

## Logging Profiles

| Profile | Output | Use Case |
|---|---|---|
| `dev` | Colored console, DEBUG level | Local development |
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

- **Entities**: Lombok `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Use `@Builder.Default` for collection defaults. `GenerationType.IDENTITY` for IDs.
- **DTOs**: Java `record` types (immutable). Use `@Valid`, `@NotBlank`, `@Size`, `@Pattern` for validation.
- **Dependency injection**: Constructor injection via `@RequiredArgsConstructor` with `final` fields. Never use `@Autowired`.
- **Controllers**: `@RestController` with `@RequestMapping` base path. Extract username via `@AuthenticationPrincipal String username`.
- **Services**: `@Service` annotated. Manual DTO mapping via private `toDto()` methods (no MapStruct).
- **Error handling**: `ResponseStatusException` for HTTP errors. `EntityNotFoundException` for 404s. No global `@ControllerAdvice`.
- **Logging**: Slf4j via Lombok `@Slf4j`.

### Database Migrations (Flyway)

- **auth-service**: `src/backend/auth-service/src/main/resources/db/migration/` — history table: `flyway_schema_history`
- **main-service**: `src/backend/main-service/src/main/resources/db/migration/` — history table: `flyway_schema_history_main`
- **Naming**: `V{YYYYMMDDHHMMSS}__{verb}_{object}.sql` using current local datetime
- **Rules**: Never edit or delete applied migrations. Always use a new timestamp.
- **Execution**: After creating migration files, **do NOT execute them automatically**. Wait for the user to start the server, which will trigger Flyway migrations on startup. Only execute manually if explicitly asked.
- **JPA**: `ddl-auto: validate` — Flyway owns the schema. Always update the Java entity to match new migrations.

### Security

- Stateless JWT. Access token: 24 hours. Refresh token: 7 days (stored in Redis).
- Tokens carry a `type` claim (`access` | `refresh`). `JwtAuthFilter` in **both** services only authenticates `type=access` — refresh tokens presented as Bearer tokens are rejected.
- Both services sign/validate JWTs independently with the same `JWT_SECRET`. The secret falls back to a built-in dev key when the env var is unset; override `JWT_SECRET` for any non-local deployment.
- Roles: `ROLE_ADMIN`, `ROLE_MEMBER`.
- `localadmin` / `password88` (ROLE_ADMIN) is seeded by `DataInitializer` in **every** environment by design (documented in `SETUP-GUIDE.txt`).
- Bot tokens are AES-256-GCM encrypted at rest (`BOT_TOKEN_ENCRYPTION_KEY`, built-in dev fallback).
- **One bot token can only power one active quiz at a time.** `QuizService.activate()` rejects a second active quiz with the same token (409 + quiz name); `TelegramBotManager` keeps a `tokenToQuizId` map as a second guard (covers app-startup restarts); `POST /api/bot/validate-token` reports `inUse`/`inUseByQuizName` so the wizard can warn before saving.
- main-service: `/api/admin/**` requires `ROLE_ADMIN` (e.g. `GET /api/admin/logs`). Session answers/photos endpoints (`/api/quizzes/sessions/**`) verify the caller owns the quiz.
- Google OAuth2 credentials stored in `app_settings` table (dynamic registration).
- The gateway is a transparent proxy (rate-limited per IP, `Origin` header removed); all authorization happens downstream in auth-service and main-service.

## Frontend Conventions

### Structure

```
src/frontend/src/
├── components/     # Reusable components (PascalCase filenames)
├── views/          # Page-level components (NameView.vue)
├── router/         # Vue Router config with navigation guards
├── stores/         # Pinia stores (Composition API pattern)
├── services/       # API client modules (exported as objects with arrow functions)
└── types/          # TypeScript interfaces (co-located by domain)
```

### Code Patterns

- **Components**: `<script setup lang="ts">` (Composition API). Template first, script second, style third.
- **Naming**: PascalCase for components, camelCase for services/stores/types/refs/functions.
- **Services**: Exported as plain objects with arrow-function methods (e.g., `quizService.list()`).
- **Types**: Exported interfaces with PascalCase, co-located in `src/types/`.
- **Store**: Composition API pattern `defineStore('name', () => { ... })`.
- **Props/Emits**: `defineProps<{...}>()` and `defineEmits<{...}>()` with generic type parameters.
- **Path alias**: `@/` maps to `./src/`. Use it consistently for all imports.
- **Styling**: Tailwind utility classes inline in templates. `slate-*` neutrals, `primary` (sky-500) for primary actions, `red-500/600` destructive, `green-*` success, `amber-*` warnings.
- **Sidebar**: Light background (`bg-white`) with pill-shaped active nav items (`bg-primary text-white rounded-full`).
- **Buttons**: Primary CTAs use `bg-primary hover:bg-primary-hover rounded-full`.
- **Modals**: `<teleport to="body">` for z-index isolation. **NEVER** add a click handler to the backdrop (overlay). Users must explicitly click the X or Cancel button to dismiss.
- **Layout**: All authenticated views wrap in `<AppLayout>` component.
- **Drag-and-drop**: `vue-draggable-plus` for list reordering, native HTML5 DnD for cross-container moves.

### Router

- Base path: `/` (local dev), configurable via `VITE_BASE_URL` env var for production
- Route meta: `requiresAuth`, `requiresGuest`, `requiresAdmin`
- Guards redirect to `/login` or `/home` based on auth state.

### API Client

- Axios instance in `src/services/api.ts`
- Request interceptor: attaches JWT, checks expiration
- Response interceptor: 401 → refresh via `/auth/refresh` (single-flight, queued) → retry; if refresh fails, clears session and redirects to login
- Vite dev proxy: `/auth`, `/api`, `/oauth2`, `/login/oauth2` → `http://localhost:8080`

## Important Notes

- No tests exist yet in any module.
- Both services have a `@RestControllerAdvice` (`GlobalExceptionHandler`) mapping `EntityNotFoundException` → 404, `ResponseStatusException`, validation failures → 400, and a generic 500 fallback.
- CORS: backend services hardcode `http://localhost:5173`; the gateway uses `allowedOriginPatterns: "*"` with credentials (mostly inert — dev uses Vite proxy, prod uses Nginx same-origin).
- The gateway is a transparent proxy (per-IP rate limiting via Redis, `Origin` header removed) — all authorization happens downstream.
- `GET /api/files/**` is publicly accessible without auth (image serving); uploads (`POST /api/files/upload`) require auth.
- Default admin account: `localadmin` / `password88` (seeded by `DataInitializer` in every environment).

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

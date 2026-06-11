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

All frontend API calls go through the gateway at `localhost:8080`. Vite proxies `/auth` and `/api` to the gateway.

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
# Infrastructure (local dev)
docker-compose -f docker-compose.dev.yml up -d

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
cp .env.example .env  # Edit with real secrets
docker compose up -d --build
```

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
- **JPA**: `ddl-auto: validate` — Flyway owns the schema. Always update the Java entity to match new migrations.

### Security

- Stateless JWT. Access token: 15 min. Refresh token: 7 days (stored in Redis).
- Both auth-service and main-service validate JWTs independently using a shared secret.
- Roles: `ROLE_ADMIN`, `ROLE_MEMBER`.
- Bot tokens are AES-256-GCM encrypted at rest.

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
- **Styling**: Tailwind utility classes inline in templates. `slate-*` neutrals, `blue-600` primary, `red-500/600` destructive, `green-*` success, `amber-*` warnings.
- **Modals**: `<teleport to="body">` for z-index isolation.
- **Layout**: All authenticated views wrap in `<AppLayout>` component.
- **Drag-and-drop**: `vue-draggable-plus` for list reordering, native HTML5 DnD for cross-container moves.

### Router

- Base path: `/tg-quiz/`
- Route meta: `requiresAuth`, `requiresGuest`, `requiresAdmin`
- Guards redirect to `/login` or `/home` based on auth state.

### API Client

- Axios instance in `src/services/api.ts`
- Request interceptor: attaches JWT, checks expiration
- Response interceptor: 401 redirects to login
- Vite dev proxy: `/auth` and `/api` → `http://localhost:8080`

## Important Notes

- No tests exist yet in any module.
- No global exception handler (`@ControllerAdvice`) exists in either backend service.
- CORS is hardcoded to `http://localhost:5173` in both backend services and the gateway.
- The gateway is a transparent proxy with no security — all auth is handled downstream.
- File uploads (`/api/files/**`) are publicly accessible without auth.
- Default admin account: `localadmin` / `szR.ir=-:Un~}RYyxZ0c` (seeded by `DataInitializer`).

## Deployment

Two deployment methods are supported:

### VPS (Docker Compose)
- `docker-compose.yml` — full stack deployment with all services containerized
- Each service has its own `Dockerfile` (backend uses multi-stage Maven build, frontend uses Node build + Nginx)
- Environment variables managed via `.env` file (template: `.env.example`)
- Frontend Nginx proxies `/auth` and `/api` to the API gateway
- Database and Redis use Docker volumes for persistence

### Render (Cloud)
- `render.yaml` — Render blueprint for one-click deployment
- Defines: 4 web services, 1 managed PostgreSQL, 1 managed Redis
- Database credentials auto-injected via `fromDatabase` (individual properties: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- Redis connection auto-injected via `fromService`
- JWT secret shared between auth and main services via `envVarGroups`
- Frontend `API_GATEWAY_URL` injected at runtime via `envsubst` in Nginx config

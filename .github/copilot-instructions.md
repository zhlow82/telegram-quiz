# GitHub Copilot Instructions — Telegram Quiz

## Project Overview
Full-stack application with a microservice backend and Vue 3 frontend.

- **Frontend**: Vue 3 + TypeScript + Pinia + Vue Router, served via Vite on port `5173`
- **API Gateway**: Spring Cloud Gateway on port `8080` — single entry point for all requests
- **Auth Service**: Spring Boot 4.0.6 on port `8081` — handles login, logout, JWT issuance, refresh tokens
- **Main Service**: Spring Boot 4.0.6 on port `8082` — protected business logic endpoints
- **Database**: PostgreSQL 17 on port `5432`
- **Cache / Token Store**: Redis 7 on port `6379`
- **DB Admin**: pgAdmin on port `5050`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3, TypeScript, Pinia, Vue Router, Axios, Vite, **Tailwind CSS v3**, **@lucide/vue**, **vue-draggable-plus** |
| Backend | Spring Boot 4.0.6, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.6), BCrypt password encoding |
| Gateway | Spring Cloud Gateway 2025.1.1 (Oakwood — required for Spring Boot 4.x), CORS via `application.yml` globalcors |
| Database | PostgreSQL 17 |
| Cache | Redis 7 |
| Build | Maven, Java 21 (Eclipse Temurin) |
| Infrastructure | Docker, Docker Compose |

---

## Project Structure

```
telegram-quiz/
├── src/
│   ├── backend/
│   │   ├── auth-service/        # Login, logout, JWT issuance (port 8081)
│   │   ├── main-service/        # Protected API endpoints (port 8082)
│   │   └── api-gateway/         # Single entry point routing (port 8080)
│   └── frontend/                # Vue 3 SPA (port 5173)
├── docker-compose.yml           # Full stack (infrastructure)
├── docker-compose.dev.yml       # Dev infrastructure only (Postgres, Redis, pgAdmin)
└── .gitignore
```

---

## Package Naming Convention
- Auth Service: `com.telegramquiz.auth`
- Main Service: `com.telegramquiz.main`
- API Gateway: `com.telegramquiz.gateway`

---

## Coding Conventions

### Java / Spring Boot
- Use **Lombok** for boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`)
- Use **constructor injection** via `@RequiredArgsConstructor` — never field injection (`@Autowired`)
- Use **DTOs** for all request/response objects — never expose JPA entities directly via API
- Use `ResponseEntity<T>` as return type in all controllers
- Use `@Valid` on request body parameters for validation
- JWT secret must be **Base64-encoded** and at least 256-bit
- All sensitive config (secrets, passwords) must be **externalized** via `application.yml` properties

### Vue / TypeScript
- Use **Composition API** with `<script setup>` — never Options API
- Use **Pinia** for all shared state — no Vuex
- Use **Vue Router** named routes for all navigation
- Axios instance is centralized in `src/services/api.ts` — never create ad-hoc axios calls
- Token is stored in `localStorage` under key `accessToken`
- Always handle 401 responses in the Axios interceptor (auto-redirect to `${BASE_URL}login` — uses `import.meta.env.BASE_URL` which resolves to `/tg-quiz/`)

### Tailwind CSS v3 (Styling)
- **Vuetify has been removed** — do NOT add `vuetify`, `vite-plugin-vuetify`, or `@mdi/font` back
- All UI is built with **Tailwind CSS v3** utility classes — no custom CSS frameworks
- Config: `tailwind.config.js` + `postcss.config.js` + `src/style.css` (contains `@tailwind` directives)
- Icons: **`@lucide/vue`** — import named icons e.g. `import { Plus, Trash2 } from '@lucide/vue'`, use as `<Plus class="w-4 h-4" />`
- Design tokens: `bg-slate-900` sidebar · `bg-blue-600 hover:bg-blue-700` primary buttons · `bg-white rounded-xl border border-slate-200` cards · `text-slate-500` muted text
- Page header pattern: `<div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">` with a `w-10 h-10 rounded-xl bg-blue-600` icon badge (white Lucide icon inside) + `<h1 class="text-2xl font-black text-slate-900 leading-tight">` title; pages with actions add `justify-between flex-wrap` and place buttons on the right
- Layout shell: `AppLayout.vue` — Tailwind-based sidebar (`bg-slate-900`) + top bar + `<main>`; main content is constrained to `max-w-6xl mx-auto`; wrap all authenticated views with `<AppLayout>`
- Modals: use `<teleport to="body">` + `v-if` overlay pattern (see `QuestionFormModal.vue`)
- Drag-and-drop: **`vue-draggable-plus`** (`VueDraggable` component) — do NOT use `vuedraggable` (Vue 2 only, broken on Vue 3.5)
- Responsive breakpoints: use Tailwind's `sm:` / `md:` / `lg:` prefixes

---

## Auth Flow
1. User POSTs credentials to `/auth/login` via API Gateway → Auth Service
2. Auth Service validates credentials, issues **access token** (15 min) + **refresh token** (7 days)
3. Refresh token is stored in **Redis** with TTL
4. Frontend stores `accessToken` in localStorage and attaches it as `Authorization: Bearer <token>` on every request
5. Main Service validates JWT using the **shared secret** only — no DB calls
6. On logout: DELETE refresh token from Redis, clear localStorage

---

## JWT Configuration
- Algorithm: **HMAC-SHA256**
- Access token expiry: **15 minutes** (`900000` ms)
- Refresh token expiry: **7 days** (`604800000` ms)
- Shared secret must be **identical** in both `auth-service` and `main-service` `application.yml`

---

## API Routes

All requests go through the API Gateway at `http://localhost:8080`.

### Auth Service (`/auth/**`)
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | No | Returns `accessToken` + `refreshToken` |
| POST | `/auth/logout` | Bearer token | Deletes refresh token from Redis |

### Main Service (`/api/**`)
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/home` | Bearer token | Home page message |
| GET | `/api/questions` | Bearer token | List all questions (ordered) |
| GET | `/api/questions/{id}` | Bearer token | Get single question |
| POST | `/api/questions` | Bearer token | Create question |
| PUT | `/api/questions/{id}` | Bearer token | Replace question |
| DELETE | `/api/questions/{id}` | Bearer token | Delete question |
| PATCH | `/api/questions/reorder` | Bearer token | Reorder — body `{ orderedIds: [Long] }` |
| POST | `/api/files/upload` | Bearer token | Upload image; returns `{ "path": "<id>" }` |
| GET | `/api/files/{id}` | Bearer token | Serve image bytes (inline) |

---

## Database Migrations (Flyway)

- **Spring Boot 4.x Flyway dependency**: Use `spring-boot-starter-flyway` (NOT bare `flyway-core`) — in Spring Boot 4.0, Flyway auto-configuration was moved out of `spring-boot-autoconfigure` into this dedicated starter. Without it, Flyway silently does not run and tables are never created. Also add `flyway-database-postgresql` for PostgreSQL dialect support.
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-flyway</artifactId>
  </dependency>
  <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
  </dependency>
  ```
- **Both services run Flyway independently** — each owns its own schema:
  - `auth-service` → migrations in `src/backend/auth-service/src/main/resources/db/migration/` → history table `flyway_schema_history`
  - `main-service` → migrations in `src/backend/main-service/src/main/resources/db/migration/` → history table `flyway_schema_history_main` (set via `spring.flyway.table` in `application.yml`)
- Naming convention: `V{YYYYMMDDHHMMSS}__{verb}_{object}.sql` (timestamp = current local datetime)
- `ddl-auto` is set to `validate` (not `update`) — Flyway owns DDL, Hibernate only validates
- Migrations run automatically on startup before the app accepts traffic
- **Never edit or delete an applied migration file** — Flyway checksums each file
- To reset local DB: `docker-compose -f docker-compose.dev.yml down -v && docker-compose -f docker-compose.dev.yml up -d`
- **Any change to the database schema (new table, new column, rename, drop, index, constraint, etc.) MUST be done via a new Flyway migration script — never by editing entities alone or running ad-hoc SQL**
- When modifying a JPA entity (adding/removing/renaming a field mapped to a column), always create the corresponding migration script at the same time

---

## Spring Profiles (Multi-Environment)

- Use Spring Boot profiles for environment-specific config: `dev`, `stg`, `prod`
- File pattern: `application.yml` (shared) + `application-{profile}.yml` (overrides)
- Activated via env var: `SPRING_PROFILES_ACTIVE=prod`
- Sensitive values in `application-prod.yml` must use env var placeholders: `${DB_PASSWORD}` — never hardcoded
- `docker-compose.dev.yml` is for local dev infrastructure only (Postgres, Redis, pgAdmin)
- Production does not use Docker Compose — use cloud-managed services (Azure / AWS)

---

## Data Models

### Question (`questions` table — main-service)
| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `orderIndex` | `int` | Display order, drag-to-reorder |
| `questionText` | `String` | Required |
| `intro` | `String` | Optional Telegram-formatted intro paragraph |
| `introBlue` | `boolean` | Whether intro renders in blue |
| `questionImagePaths` | `List<String>` | JSONB — list of `ImageBlob` IDs as strings |
| `options` | `List<String>` | JSONB — Telegram-formatted answer options |
| `answer` | `String` | Correct answer text |
| `expectPhoto` | `boolean` | Whether question expects a photo response |
| `isBriefing` | `boolean` | Marks question as a briefing slide (no answer) |
| `hintText` | `String` | Optional hint (Telegram-formatted) |
| `hintImagePaths` | `List<String>` | JSONB — hint image IDs |
| `explanationTexts` | `List<String>` | JSONB — post-answer explanation paragraphs |
| `explanationImagePaths` | `List<String>` | JSONB — explanation image IDs |
| `createdAt` / `updatedAt` | `LocalDateTime` | Set via `@PrePersist` / `@PreUpdate` |

> **Lombok note**: `boolean isBriefing` generates `isBriefing()` getter (not `getIsBriefing()`). Use `isBriefing()` in Java code.

### ImageBlob (`image_blobs` table — main-service)
Stores uploaded images as binary in PostgreSQL. Fields: `id` (Long PK), `contentType` (String), `data` (byte[]).
Upload via `POST /api/files/upload` (multipart), serve via `GET /api/files/{id}` (returns raw bytes with `Content-Type` header).
Frontend stores image IDs as strings in `questionImagePaths` / `hintImagePaths` / `explanationImagePaths`.

---

## Default Dev Credentials
- **App login**: `zhlow` / `password88` (seeded on startup by `DataInitializer`)
- **pgAdmin**: `admin@telegramquiz.com` / `admin`
- **PostgreSQL**: host `localhost:5432`, db/user/pass `postgres`
- **Redis**: `localhost:6379` (no auth in dev)

---

## Running Locally

### 1. Start dev infrastructure
```bash
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Start backend services (separate terminals)
```bash
# Auth Service
cd src/backend/auth-service && mvn spring-boot:run

# Main Service
cd src/backend/main-service && mvn spring-boot:run

# API Gateway
cd src/backend/api-gateway && mvn spring-boot:run
```

### 3. Start frontend
```bash
cd src/frontend && npm run dev
```

Open `http://localhost:5173/tg-quiz/`

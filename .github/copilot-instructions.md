# GitHub Copilot Instructions — ZH Template

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
| Frontend | Vue 3, TypeScript, Pinia, Vue Router, Axios, Vite |
| Backend | Spring Boot 4.0.6, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.6), BCrypt password encoding |
| Gateway | Spring Cloud Gateway 2025.0.0 |
| Database | PostgreSQL 17 |
| Cache | Redis 7 |
| Build | Maven, Java 21 (Eclipse Temurin) |
| Infrastructure | Docker, Docker Compose |

---

## Project Structure

```
zh-template/
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
- Auth Service: `com.zhtemplate.auth`
- Main Service: `com.zhtemplate.main`
- API Gateway: `com.zhtemplate.gateway`

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
- Always handle 401 responses in the Axios interceptor (auto-redirect to `/login`)

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

| Method | Path | Service | Auth Required |
|---|---|---|---|
| POST | `/auth/login` | auth-service | No |
| POST | `/auth/logout` | auth-service | Yes (Bearer token) |
| GET | `/api/home` | main-service | Yes (Bearer token) |

---

## Database Migrations (Flyway)

- Flyway is configured in `auth-service` only — it owns the schema
- Migration files live in `src/backend/auth-service/src/main/resources/db/migration/`
- Naming convention: `V{YYYYMMDDHHMMSS}__{verb}_{object}.sql` (timestamp = current local datetime)
- `ddl-auto` is set to `validate` (not `update`) — Flyway owns DDL, Hibernate only validates
- Migrations run automatically on startup before the app accepts traffic
- **Never edit or delete an applied migration file** — Flyway checksums each file
- To reset local DB: `docker-compose -f docker-compose.dev.yml down -v && docker-compose -f docker-compose.dev.yml up -d`
- Migration history table: `flyway_schema_history` in PostgreSQL

---

## Default Dev Credentials
- **App login**: `zhlow` / `password88` (seeded on startup by `DataInitializer`)
- **pgAdmin**: `admin@zhtemplate.com` / `admin`
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

Open `http://localhost:5173`

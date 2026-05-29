# Telegram Quiz

A full-stack application template built with a microservice backend and Vue 3 frontend, featuring JWT authentication backed by PostgreSQL and Redis.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3, TypeScript, Pinia, Vue Router, Axios, Vite, **Tailwind CSS v3**, **@lucide/vue**, **vue-draggable-plus** |
| Backend | Spring Boot 4.0.6, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.6), BCrypt |
| Gateway | Spring Cloud Gateway 2025.1.1 (Oakwood) |
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
│   │   ├── auth-service/        # Login, logout, JWT issuance       (port 8081)
│   │   ├── main-service/        # Protected API endpoints            (port 8082)
│   │   └── api-gateway/         # Single entry point routing         (port 8080)
│   └── frontend/                # Vue 3 SPA                          (port 5173)
├── docker-compose.yml           # Full infrastructure
├── docker-compose.dev.yml       # Dev infrastructure only
└── .gitignore
```

---

## Prerequisites

The following tools must be installed before running the project.

### 1. Docker Desktop
Used to run PostgreSQL, Redis, and pgAdmin as containers.

- Download: https://www.docker.com/products/docker-desktop
- Verify: `docker --version`

### 2. Java 21 (Eclipse Temurin)
Required to compile and run all Spring Boot services.

- Download: https://adoptium.net/temurin/releases/?version=21
- Choose: **Windows x64 `.msi` installer**
- After install, set `JAVA_HOME` to the JDK folder (e.g. `C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot`) and add `%JAVA_HOME%\bin` to your system `PATH`
- Verify: `java -version`

> Or install via winget:
> ```powershell
> winget install EclipseAdoptium.Temurin.21.JDK
> ```

### 3. Maven 3.9+
Used to build and run the Spring Boot services.

- Download: https://maven.apache.org/download.cgi — get the **Binary zip archive**
- Extract to a folder (e.g. `C:\tools\maven`)
- Set `MAVEN_HOME` to that folder and add `%MAVEN_HOME%\bin` to your system `PATH`
- Verify: `mvn -version`

### 4. Node.js 24 LTS + npm
Used to run the Vue 3 frontend.

- Download: https://nodejs.org — choose **LTS**
- npm is bundled with Node.js
- The Windows installer adds Node.js to `PATH` automatically; if `node` or `npm` are not found after install, manually add `C:\Program Files\nodejs` to your system `PATH`
- Verify: `node --version` and `npm --version`

> Or install via winget:
> ```powershell
> winget install OpenJS.NodeJS.LTS
> ```

### 5. Visual Studio Code
Recommended editor with the following extensions:

- **Extension Pack for Java** (`vscjava.vscode-java-pack`) — Java language support, debugger, Maven
- **Spring Boot Extension Pack** (`vmware.vscode-boot-dev-pack`) — Spring Boot run/debug support
- **Vue - Official** (`Vue.volar`) — Vue 3 + TypeScript support

---

### Quick verification checklist

Run these in a terminal to confirm everything is installed:

```powershell
docker --version      # Docker Desktop
java -version         # Java 21
mvn -version          # Maven 3.9+
node --version        # Node.js 24
npm --version         # npm 11+
```

---

## Getting Started

### 1. Start dev infrastructure (PostgreSQL, Redis, pgAdmin)

```bash
docker-compose -f docker-compose.dev.yml up -d
```

| Service | URL |
|---|---|
| PostgreSQL | `localhost:5432` |
| pgAdmin | http://localhost:5050 |
| Redis | `localhost:6379` |

pgAdmin login: `admin@telegramquiz.com` / `admin`

PostgreSQL connection: host `localhost`, port `5432`, db/user/pass `postgres`

### 2. Start backend services

Open three separate terminals:

```bash
# Auth Service
cd src/backend/auth-service
mvn spring-boot:run
```

```bash
# Main Service
cd src/backend/main-service
mvn spring-boot:run
```

```bash
# API Gateway
cd src/backend/api-gateway
mvn spring-boot:run
```

### 3. Start frontend

```bash
cd src/frontend
npm install
npm run dev
```

### 4. Open the app

Navigate to http://localhost:5173/tg-quiz/

Login with:
- **Username**: `zhlow`
- **Password**: `password88`

---

## Service Ports

| Service | Port |
|---|---|
| Vue Frontend | 5173 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| Main Service | 8082 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| pgAdmin | 5050 |

---

## API Endpoints

All requests go through the API Gateway at `http://localhost:8080`.

### Auth
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/login` | No | Login, returns `accessToken` + `refreshToken` |
| `POST` | `/auth/logout` | Bearer token | Logout, invalidates refresh token in Redis |

### Questions
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/questions` | Bearer token | List all questions (ordered) |
| `GET` | `/api/questions/{id}` | Bearer token | Get single question |
| `POST` | `/api/questions` | Bearer token | Create question |
| `PUT` | `/api/questions/{id}` | Bearer token | Replace question |
| `DELETE` | `/api/questions/{id}` | Bearer token | Delete question |
| `PATCH` | `/api/questions/reorder` | Bearer token | Reorder — body `{ "orderedIds": [1, 2, 3] }` |

### Files
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/files/upload` | Bearer token | Upload image (multipart); returns `{ "path": "<id>" }` |
| `GET` | `/api/files/{id}` | Bearer token | Serve image bytes inline |

### Home
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/home` | Bearer token | Home page message |

### Login example

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "zhlow", "password": "password88"}'
```

Response:
```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenType": "Bearer"
}
```

---

## Auth Flow

```
Frontend → API Gateway (8080)
              ├── /auth/** → Auth Service (8081) → PostgreSQL + Redis
              └── /api/**  → Main Service (8082) → validates JWT locally
```

- **Access token**: 15 minutes, stateless JWT
- **Refresh token**: 7 days, stored in Redis (deleted on logout)
- Main Service validates JWT using the shared secret — no DB calls required

---

## Configuration

Key settings in `application.yml` per service:

| Setting | Location | Notes |
|---|---|---|
| `jwt.secret` | auth-service, main-service | Must be identical in both |
| `jwt.expiration` | auth-service | Access token TTL (ms) |
| `jwt.refresh-expiration` | auth-service | Refresh token TTL (ms) |
| `spring.datasource.*` | auth-service | PostgreSQL connection |
| `spring.data.redis.*` | auth-service | Redis connection |

> **Security note**: Change `jwt.secret` before deploying to any non-local environment and store it as an environment variable, not in source code.

---

## Database Migrations (Flyway)

Schema changes are managed by **Flyway**, running independently in both `auth-service` and `main-service`. Migrations run automatically on startup before the app accepts requests.

### Migration locations

```
src/backend/auth-service/src/main/resources/db/migration/
  └─ History table: flyway_schema_history
  └─ Manages: users, user_roles tables

src/backend/main-service/src/main/resources/db/migration/
  └─ History table: flyway_schema_history_main
  └─ Manages: questions, image_blobs tables
```

### Naming convention

```
V{YYYYMMDDHHMMSS}__{verb}_{object}.sql
```

Use the **current local datetime** as the timestamp when creating a new file. This prevents version conflicts when multiple developers create migrations on different branches simultaneously.

Examples:
- `V20260530091500__add_email_to_users.sql`
- `V20260601120000__create_profiles_table.sql`
- `V20260602083000__add_index_on_users_username.sql`

### Rules

| Rule | Reason |
|---|---|
| Never edit an applied migration file | Flyway checksums each file — changed content causes startup failure |
| Never delete a migration file | Same checksum issue |
| Always use a new timestamp | Versions must be unique and increasing |

### Adding a new migration

1. Decide which service owns the table you're changing (`auth-service` → users schema, `main-service` → questions/files schema)
2. Create a new `.sql` file in that service's `db/migration/` folder using the timestamp format
3. Write your DDL (ALTER TABLE, CREATE TABLE, etc.)
4. Restart the relevant service — Flyway applies it automatically
5. Update the corresponding Java entity to match

### Checking migration history

Connect to PostgreSQL and query:
```sql
-- auth-service migrations
SELECT version, description, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;

-- main-service migrations
SELECT version, description, installed_on, success FROM flyway_schema_history_main ORDER BY installed_rank;
```

### If a migration fails

For local dev: `docker-compose -f docker-compose.dev.yml down -v && docker-compose -f docker-compose.dev.yml up -d` resets the DB and Flyway re-runs all migrations from scratch.

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
│   │   │   └── Dockerfile
│   │   ├── main-service/        # Protected API endpoints            (port 8082)
│   │   │   └── Dockerfile
│   │   └── api-gateway/         # Single entry point routing         (port 8080)
│   │       └── Dockerfile
│   └── frontend/                # Vue 3 SPA                          (port 5173)
│       ├── Dockerfile
│       ├── nginx.conf           # Nginx reverse proxy config
│       └── entrypoint.sh        # Runtime env var substitution
├── docker-compose.yml           # Full stack deployment (VPS)
├── .devcontainer/                 # VS Code dev container (Postgres, Redis, pgAdmin, Java 21, Node 20)
├── render.yaml                  # Render cloud deployment blueprint
├── start.bat                    # End-user: build & start full stack (Docker Desktop only)
├── stop.bat                     # End-user: stop services (data preserved)
├── zip.bat                      # End-user: create slim distribution zip
├── SETUP-GUIDE.txt              # Step-by-step guide for end users
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

### 1. Open in Dev Container

In VS Code, click **"Reopen in Container"** when prompted (requires the [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)). This automatically starts PostgreSQL, Redis, and pgAdmin, and installs frontend dependencies.

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

Navigate to http://localhost:5173/

Login with:
- **Username**: `localadmin`
- **Password**: `password88`

> `localadmin` is the default admin account seeded by `DataInitializer` on first startup.

---

### Run it on your own machine — no dev tools needed

If you just want to *host* the app locally (no Java/Node/Maven installation), the repo ships a zero-config flow that only needs **Docker Desktop**:

1. Run `zip.bat` to build a slim `telegram-quiz.zip` distribution package
2. Give the zip + `SETUP-GUIDE.txt` to the end user
3. End user: install Docker Desktop → extract zip → double-click `start.bat`
4. App is served at **http://localhost/tg-quiz** — log in with `localadmin` / `password88`

- `start.bat` builds & starts the full stack, waits until the app responds, then prints Ready
- `stop.bat` stops all services (data preserved); `docker compose down -v` fully resets data
- First start needs an internet connection and takes a few minutes (image downloads + builds)
- Full walkthrough in `SETUP-GUIDE.txt` (including a quick guide on creating questions and running quizzes)

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
| `POST` | `/auth/refresh` | No | Body `{ refreshToken }` — returns new `{ accessToken }` |
| `POST` | `/auth/logout` | Bearer token | Logout, invalidates refresh token in Redis |
| `GET` | `/auth/me` | Bearer token | Get current user profile |
| `PATCH` | `/auth/profile` | Bearer token | Update first name / last name |
| `POST` | `/auth/change-password` | Bearer token | Change password (local accounts only) |
| `GET` | `/auth/settings/branding` | No | Get app branding (name, welcome text, logo URL) |
| `GET` | `/auth/oauth2/configured` | No | Whether Google OAuth2 credentials are configured |
| `GET` | `/auth/users/search?q=` | Bearer token | Search users by username (folder invites) |
| `POST` | `/auth/oauth2/complete` | No | Complete Google OAuth2 registration with invitation code |

### Admin (ROLE_ADMIN only)
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/auth/admin/users` | ROLE_ADMIN | List all users |
| `POST` | `/auth/admin/users` | ROLE_ADMIN | Create local user |
| `PATCH` | `/auth/admin/users/{id}/role` | ROLE_ADMIN | Set role (`ROLE_ADMIN` or `ROLE_MEMBER`) |
| `PATCH` | `/auth/admin/users/{id}/profile` | ROLE_ADMIN | Update first/last name for any user |
| `PATCH` | `/auth/admin/users/{id}/password` | ROLE_ADMIN | Reset password for local accounts |
| `PATCH` | `/auth/admin/users/{id}/activate` | ROLE_ADMIN | Activate user |
| `PATCH` | `/auth/admin/users/{id}/deactivate` | ROLE_ADMIN | Deactivate user |
| `DELETE` | `/auth/admin/users/{id}` | ROLE_ADMIN | Permanently delete user |
| `GET` | `/auth/admin/invitation-codes` | ROLE_ADMIN | List invitation codes |
| `POST` | `/auth/admin/invitation-codes` | ROLE_ADMIN | Generate invitation code |
| `DELETE` | `/auth/admin/invitation-codes/{id}` | ROLE_ADMIN | Deactivate invitation code (soft) |
| `DELETE` | `/auth/admin/invitation-codes/{id}/permanent` | ROLE_ADMIN | Permanently delete invitation code |
| `PATCH` | `/auth/admin/invitation-codes/{id}/activate` | ROLE_ADMIN | Re-activate invitation code |
| `GET` | `/auth/admin/settings/google` | ROLE_ADMIN | Get Google OAuth2 settings |
| `PUT` | `/auth/admin/settings/google` | ROLE_ADMIN | Save Google OAuth2 credentials |
| `PUT` | `/auth/admin/settings/branding` | ROLE_ADMIN | Save branding settings (app name, welcome text, logo) |
| `GET` | `/auth/admin/logs` | ROLE_ADMIN | View in-memory application logs (auth-service) |

### Quiz Sessions
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/quizzes/{id}/sessions` | Bearer (quiz owner) | List participant sessions for a quiz |
| `GET` | `/api/quizzes/sessions/{sessionId}/answers` | Bearer (quiz owner) | View a participant's individual answers |
| `GET` | `/api/quizzes/sessions/{sessionId}/photos?fileId=` | Bearer (quiz owner) | Download a participant's photo answer |

### Questions
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/questions` | Bearer token | List all accessible questions (ordered) |
| `GET` | `/api/questions/{id}` | Bearer token | Get single question |
| `POST` | `/api/questions` | Bearer token | Create question |
| `PUT` | `/api/questions/{id}` | Bearer token | Replace question |
| `DELETE` | `/api/questions/{id}` | Bearer token | Delete question |
| `PATCH` | `/api/questions/reorder` | Bearer token | Reorder — body `{ "orderedIds": [1, 2, 3] }` |
| `PATCH` | `/api/questions/{id}/folder` | Bearer token | Move question to folder — body `{ "folderId": 1 }` |

### Folders
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/folders` | Bearer token | List owned + shared folders |
| `POST` | `/api/folders` | Bearer token | Create folder |
| `PATCH` | `/api/folders/{id}/name` | Bearer token | Rename folder |
| `DELETE` | `/api/folders/{id}` | Bearer token | Delete folder |
| `PATCH` | `/api/folders/reorder` | Bearer token | Reorder owned folders |
| `GET` | `/api/folders/{id}/members` | Bearer token | List folder members |
| `POST` | `/api/folders/{id}/members` | Bearer token | Invite user to folder |
| `PATCH` | `/api/folders/{id}/members/{username}/role` | Bearer token | Change member role |
| `DELETE` | `/api/folders/{id}/members/{username}` | Bearer token | Remove member |

### Quizzes
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/quizzes` | Bearer token | List quizzes |
| `GET` | `/api/quizzes/{id}` | Bearer token | Get quiz detail |
| `POST` | `/api/quizzes` | Bearer token | Create quiz |
| `PUT` | `/api/quizzes/{id}` | Bearer token | Update quiz |
| `DELETE` | `/api/quizzes/{id}` | Bearer token | Delete quiz |
| `POST` | `/api/quizzes/{id}/activate` | Bearer token | Activate quiz (start bot). Fails with 409 if the bot token already runs another active quiz |
| `POST` | `/api/quizzes/{id}/stop` | Bearer token | Stop quiz |
| `GET` | `/api/quizzes/{id}/sessions` | Bearer (quiz owner) | List participant sessions |
| `POST` | `/api/bot/validate-token` | Bearer token | Validate a Telegram bot token (calls Telegram `getMe`). Body `{ token, excludeQuizId? }`; response includes `inUse` / `inUseByQuizName` |

### Quiz Session Answers
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/quizzes/sessions/{sessionId}/answers` | Bearer (quiz owner) | View participant's individual answers |
| `GET` | `/api/quizzes/sessions/{sessionId}/photos` | Bearer (quiz owner) | Download a participant's photo answer |

### Admin Logs
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/auth/admin/logs` | ROLE_ADMIN | View in-memory application logs (auth-service) |
| `GET` | `/api/admin/logs` | ROLE_ADMIN | View in-memory application logs (main-service) |

### Files
| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/files/upload` | Bearer token | Upload image (multipart); returns `{ "path": "<id>" }` |
| `GET` | `/api/files/{id}` | No (public) | Serve image bytes inline |

### Home
| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/home` | Bearer token | Home page message |

### Login example

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "localadmin", "password": "password88"}'
```

Response:
```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>"
}
```

---

## Auth Flow

```
Frontend → API Gateway (8080)
              ├── /auth/**   → Auth Service (8081) → PostgreSQL + Redis
              ├── /oauth2/** → Auth Service (8081) (Google OAuth2)
              └── /api/**    → Main Service (8082) → validates JWT locally
```

- **Access token**: 24 hours, stateless JWT
- **Refresh token**: 7 days, stored in Redis (deleted on logout)
- Tokens carry a `type` claim (`access` / `refresh`); both services reject refresh tokens presented as Bearer tokens
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

> **Security note**: The JWT secret falls back to a built-in development key when `JWT_SECRET` is unset. Change it before exposing the app beyond your own machine (set `JWT_SECRET` as an environment variable or in `.env`).

---

## Deployment

The project supports two deployment methods:

### Method 1: VPS with Docker Compose

Deploy the entire stack on a Linux VPS (DigitalOcean, Hetzner, Linode, etc.) using `docker-compose.yml` (compose project name: `telegram-quiz`).

**Prerequisites:**
- A Linux VPS with Docker and Docker Compose installed
- SSH access to the server

**Steps:**

1. SSH into your VPS and install Docker (if not already installed):
   ```bash
   sudo apt update && sudo apt install docker.io docker-compose-v2 -y
   ```

2. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd telegram-quiz
   ```

3. Build and start all services:
   ```bash
   docker compose up -d --build
   ```

**Services included:**
| Service | Port | Description |
|---|---|---|
| Frontend (Nginx) | 80 | Vue 3 SPA served at `/tg-quiz/` (configurable via `VITE_BASE_URL`) |
| API Gateway | 8080 | Spring Cloud Gateway |
| Auth Service | 8081 | JWT authentication |
| Main Service | 8082 | Business logic |
| PostgreSQL | 5432 | Database |
| Redis | 6379 | Token cache |

**Configuration:** All environment variables use defaults defined in `docker-compose.yml` and `application.yml`. You have two options to customize values:

**Option A: Set environment variables on the server**
```bash
# Linux (add to /etc/environment or ~/.bashrc)
export POSTGRES_PASSWORD=your-secure-password
export JWT_SECRET=your-jwt-secret
export BOT_TOKEN_ENCRYPTION_KEY=your-encryption-key

# Then run
docker compose up -d --build
```

**Option B: Use a `.env` file (optional)**
```bash
# Create .env file
cat > .env << EOF
POSTGRES_PASSWORD=your-secure-password
JWT_SECRET=your-jwt-secret
BOT_TOKEN_ENCRYPTION_KEY=your-encryption-key
EOF

# Docker Compose reads .env automatically
docker compose up -d --build
```

**Available variables:**
| Variable | Description | Default |
|---|---|---|
| `POSTGRES_DB` | Database name | `postgres` |
| `POSTGRES_USER` | Database username | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | Base64-encoded JWT signing secret | (built-in dev key) |
| `BOT_TOKEN_ENCRYPTION_KEY` | Base64-encoded AES-256 key for bot token encryption | (built-in dev key) |
| `FRONTEND_URL` | Public origin used for the OAuth2 post-login redirect | `http://localhost/tg-quiz` |

> **Important:** For production, change the default passwords and secrets. Never commit `.env` files or hardcoded secrets to Git.

---

### Method 2: Render (Cloud Platform)

Deploy using `render.yaml` blueprint on [Render](https://render.com). Render manages infrastructure, databases, and secrets automatically.

**Steps:**

1. Push your code to GitHub.

2. In the Render dashboard:
   - Click **New** → **Blueprint**
   - Connect your GitHub repository
   - Render reads `render.yaml` and shows a preview of all services

3. Click **Apply** — Render creates:
   - 4 Web Services (auth, main, gateway, frontend)
   - 1 Managed PostgreSQL database
   - 1 Managed Redis instance
   - Environment variables wired automatically between services

**What `render.yaml` defines:**
- Backend services built from their respective Dockerfiles
- Database and Redis credentials auto-injected via `fromDatabase` and `fromService`
- JWT secret auto-generated and shared between auth and main services
- Frontend Nginx configured to proxy API requests to the gateway

**Updating services:**
- Push changes to GitHub → Render auto-detects and redeploys
- Modify `render.yaml` → Render prompts to sync changes

**Cost estimate:** ~$12-15/month (free tier available for static sites and databases)

---

## Database Migrations (Flyway)

Schema changes are managed by **Flyway**, running independently in both `auth-service` and `main-service`. Migrations run automatically on startup before the app accepts requests.

### Migration locations

```
src/backend/auth-service/src/main/resources/db/migration/
  └─ History table: flyway_schema_history
  └─ Manages: users, user_roles, invitation_codes, app_settings tables

src/backend/main-service/src/main/resources/db/migration/
  └─ History table: flyway_schema_history_main
  └─ Manages: questions, image_blobs, folders, folder_members,
              quizzes, quiz_questions, quiz_sessions, quiz_session_answers tables
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

For local dev: `docker compose -f .devcontainer/docker-compose.yml down -v && docker compose -f .devcontainer/docker-compose.yml up -d` resets the DB and Flyway re-runs all migrations from scratch.

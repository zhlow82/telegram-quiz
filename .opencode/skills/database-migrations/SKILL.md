---
name: database-migrations
description: Use when creating or modifying database schema via Flyway migrations. Covers migration file naming, SQL conventions, entity synchronization rules, and the relationship between auth-service and main-service schemas.
---

# Database Migrations (Flyway)

## Migration Locations

| Service | Path | History Table |
|---|---|---|
| auth-service | `src/backend/auth-service/src/main/resources/db/migration/` | `flyway_schema_history` |
| main-service | `src/backend/main-service/src/main/resources/db/migration/` | `flyway_schema_history_main` |

## Naming Convention

```
V{YYYYMMDDHHMMSS}__{verb}_{object}.sql
```

Use the **current local datetime** when creating a new migration:

```bash
# Get current timestamp for migration name
powershell: Get-Date -Format "yyyyMMddHHmmss"
```

Examples:
- `V20260609143000__add_status_to_quizzes.sql`
- `V20260609150000__create_quiz_results_table.sql`
- `V20260609153000__add_index_on_quiz_sessions.sql`

## Rules

1. **Never edit an applied migration** — Flyway checksums each file; changes cause startup failure
2. **Never delete a migration** — Same checksum issue
3. **Always use a new timestamp** — Versions must be unique and increasing
4. **Always update the Java entity** — JPA `ddl-auto: validate` requires entity to match schema

## Migration Template

```sql
-- Add new column
ALTER TABLE table_name ADD COLUMN new_column VARCHAR(255);

-- Add column with default
ALTER TABLE table_name ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Add foreign key
ALTER TABLE child_table ADD COLUMN parent_id BIGINT REFERENCES parent_table(id) ON DELETE CASCADE;

-- Add index
CREATE INDEX idx_table_column ON table_name(column_name);

-- Add unique constraint
ALTER TABLE table_name ADD CONSTRAINT uk_table_column UNIQUE (column_name);

-- Create new table
CREATE TABLE new_table (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## Schema Ownership

| Service | Tables |
|---|---|
| auth-service | `users`, `user_roles`, `invitation_codes`, `app_settings` |
| main-service | `questions`, `quizzes`, `quiz_questions`, `quiz_sessions`, `image_blobs`, `folders`, `folder_members` |

## Entity Synchronization

After creating a migration, update the corresponding Java entity:

```java
// If migration adds:
// ALTER TABLE questions ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

// Then add to Question.java:
@Column(name = "status", nullable = false)
@Builder.Default
private String status = "DRAFT";
```

## Checking Migration History

```sql
-- auth-service
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank;

-- main-service
SELECT version, description, installed_on, success 
FROM flyway_schema_history_main 
ORDER BY installed_rank;
```

## Resetting Database (Dev Only)

```bash
docker compose -f .devcontainer/docker-compose.yml down -v
docker compose -f .devcontainer/docker-compose.yml up -d
```

This drops all data and Flyway re-runs all migrations from scratch.

## Common Patterns

**JSONB columns:**
```sql
ALTER TABLE questions ADD COLUMN content_blocks JSONB NOT NULL DEFAULT '[]'::jsonb;
```

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "content_blocks", columnDefinition = "jsonb")
@Builder.Default
private List<ContentBlock> contentBlocks = new ArrayList<>();
```

**Enum columns:**
```sql
ALTER TABLE quizzes ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
```

```java
@Column(name = "status", nullable = false)
@Builder.Default
private QuizStatus status = QuizStatus.DRAFT;
```

**Soft delete (boolean flag):**
```sql
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
```

```java
@Column(name = "active", nullable = false)
@Builder.Default
private Boolean active = true;
```

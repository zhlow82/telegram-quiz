CREATE TABLE IF NOT EXISTS folder_members (
    id          BIGSERIAL    PRIMARY KEY,
    folder_id   BIGINT       NOT NULL REFERENCES folders(id) ON DELETE CASCADE,
    username    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    invited_by  VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_folder_members UNIQUE (folder_id, username)
);

CREATE INDEX IF NOT EXISTS idx_folder_members_username  ON folder_members(username);
CREATE INDEX IF NOT EXISTS idx_folder_members_folder_id ON folder_members(folder_id);

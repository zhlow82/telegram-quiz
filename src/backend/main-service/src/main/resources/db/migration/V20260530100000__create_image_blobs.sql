CREATE TABLE image_blobs (
    id           BIGSERIAL PRIMARY KEY,
    data         BYTEA NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

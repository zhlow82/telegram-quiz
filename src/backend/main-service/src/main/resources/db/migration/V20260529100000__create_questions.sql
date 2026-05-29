CREATE TABLE IF NOT EXISTS questions (
    id                      BIGSERIAL PRIMARY KEY,
    order_index             INTEGER NOT NULL DEFAULT 0,
    category                VARCHAR(100),
    question_text           TEXT NOT NULL DEFAULT '',
    intro                   TEXT,
    intro_blue              BOOLEAN NOT NULL DEFAULT FALSE,
    question_image_path     VARCHAR(500),
    options                 JSONB NOT NULL DEFAULT '[]',
    answer                  VARCHAR(500),
    expect_photo            BOOLEAN NOT NULL DEFAULT FALSE,
    hint_text               TEXT,
    hint_image_path         VARCHAR(500),
    explanation_texts       JSONB NOT NULL DEFAULT '[]',
    explanation_image_paths JSONB NOT NULL DEFAULT '[]',
    is_visible              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_questions_order    ON questions (order_index);
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions (category);
CREATE INDEX IF NOT EXISTS idx_questions_visible  ON questions (is_visible);

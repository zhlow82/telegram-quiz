CREATE TABLE quiz_sessions (
    id            BIGSERIAL PRIMARY KEY,
    quiz_id       BIGINT       NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    telegram_user_id   BIGINT  NOT NULL,
    telegram_username  VARCHAR(100),
    telegram_first_name VARCHAR(255),
    score              INT     NOT NULL DEFAULT 0,
    total_questions    INT     NOT NULL,
    passed             BOOLEAN NOT NULL DEFAULT FALSE,
    status             VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at       TIMESTAMP
);

CREATE INDEX idx_quiz_sessions_quiz_id ON quiz_sessions(quiz_id);

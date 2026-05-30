CREATE TABLE quizzes (
    id                        BIGSERIAL PRIMARY KEY,
    name                      VARCHAR(255)  NOT NULL,
    bot_token                 TEXT          NOT NULL,
    time_per_question_seconds INTEGER       NOT NULL DEFAULT 30,
    pass_score_percent        INTEGER       NOT NULL DEFAULT 60,
    status                    VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_at                TIMESTAMP     NOT NULL
);

CREATE TABLE quiz_questions (
    id           BIGSERIAL PRIMARY KEY,
    quiz_id      BIGINT  NOT NULL REFERENCES quizzes(id)   ON DELETE CASCADE,
    question_id  BIGINT  NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    order_index  INTEGER NOT NULL,
    UNIQUE (quiz_id, question_id)
);

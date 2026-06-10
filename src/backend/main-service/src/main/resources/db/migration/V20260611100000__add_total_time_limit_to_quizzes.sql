ALTER TABLE quizzes
    ADD COLUMN total_time_limit_seconds INTEGER NOT NULL DEFAULT 0,
    ALTER COLUMN time_per_question_seconds SET DEFAULT 0;

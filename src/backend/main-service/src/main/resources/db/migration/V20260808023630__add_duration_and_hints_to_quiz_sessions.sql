-- Add quiz-taking duration and hint usage to quiz sessions
ALTER TABLE quiz_sessions ADD COLUMN duration_ms BIGINT;
ALTER TABLE quiz_sessions ADD COLUMN hints_used INTEGER;

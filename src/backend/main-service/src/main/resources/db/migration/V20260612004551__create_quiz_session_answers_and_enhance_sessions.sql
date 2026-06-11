-- Create quiz_session_answers table to store individual participant responses
CREATE TABLE quiz_session_answers (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    selected_answer VARCHAR(500),
    is_correct BOOLEAN,
    photo_file_id VARCHAR(500),
    photo_caption TEXT,
    response_time_ms INTEGER,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_session_answers_session_id ON quiz_session_answers(session_id);
CREATE INDEX idx_session_answers_question_id ON quiz_session_answers(question_id);

-- Enhance quiz_sessions with resume and tracking columns
ALTER TABLE quiz_sessions ADD COLUMN current_question_index INTEGER NOT NULL DEFAULT 0;
ALTER TABLE quiz_sessions ADD COLUMN last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE quiz_sessions ADD COLUMN abandoned_at TIMESTAMPTZ;

-- Add ABANDONED to the status enum (stored as VARCHAR, so just update application logic)

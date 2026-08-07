-- Allow questions to be deleted even after they have been answered in a quiz.
-- Previously the FK (NO ACTION) made any answered question permanently undeletable.
ALTER TABLE quiz_session_answers
    DROP CONSTRAINT quiz_session_answers_question_id_fkey,
    ADD CONSTRAINT quiz_session_answers_question_id_fkey
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE;

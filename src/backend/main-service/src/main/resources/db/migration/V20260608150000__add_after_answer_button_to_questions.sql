ALTER TABLE questions
    ADD COLUMN IF NOT EXISTS after_answer_button_text VARCHAR(255);

ALTER TABLE questions
    ADD COLUMN IF NOT EXISTS show_after_answer_button BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE questions
SET after_answer_button_text = 'READY'
WHERE after_answer_button_text IS NULL;

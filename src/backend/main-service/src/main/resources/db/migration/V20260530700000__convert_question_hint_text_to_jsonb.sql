-- Migrate question_text (TEXT) → question_texts (JSONB array)
ALTER TABLE questions ADD COLUMN question_texts JSONB NOT NULL DEFAULT '[]'::jsonb;
UPDATE questions SET question_texts = jsonb_build_array(question_text) WHERE question_text IS NOT NULL AND question_text != '';
ALTER TABLE questions DROP COLUMN question_text;

-- Migrate hint_text (TEXT) → hint_texts (JSONB array)
ALTER TABLE questions ADD COLUMN hint_texts JSONB NOT NULL DEFAULT '[]'::jsonb;
UPDATE questions SET hint_texts = jsonb_build_array(hint_text) WHERE hint_text IS NOT NULL AND hint_text != '';
ALTER TABLE questions DROP COLUMN hint_text;

DROP INDEX IF EXISTS idx_questions_category;
ALTER TABLE questions DROP COLUMN IF EXISTS category;

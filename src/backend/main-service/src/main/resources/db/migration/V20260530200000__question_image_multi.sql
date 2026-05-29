ALTER TABLE questions
    ADD COLUMN question_image_paths  JSONB NOT NULL DEFAULT '[]',
    ADD COLUMN hint_image_paths      JSONB NOT NULL DEFAULT '[]';

-- Migrate existing single-path data into the new list columns
UPDATE questions SET question_image_paths = jsonb_build_array(question_image_path) WHERE question_image_path IS NOT NULL AND question_image_path <> '';
UPDATE questions SET hint_image_paths     = jsonb_build_array(hint_image_path)     WHERE hint_image_path     IS NOT NULL AND hint_image_path     <> '';

ALTER TABLE questions
    DROP COLUMN question_image_path,
    DROP COLUMN hint_image_path;

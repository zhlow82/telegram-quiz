-- Unify separate text/image columns into ordered content block arrays
-- Runs after V20260530700000 which created question_texts and hint_texts JSONB columns

ALTER TABLE questions ADD COLUMN question_blocks JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE questions ADD COLUMN hint_blocks JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE questions ADD COLUMN explanation_blocks JSONB NOT NULL DEFAULT '[]'::jsonb;

-- Migrate: texts first, then images (preserves existing content; order is text-then-image for migration)
UPDATE questions SET question_blocks = (
    COALESCE(
        (SELECT jsonb_agg(jsonb_build_object('type', 'text', 'content', val))
         FROM jsonb_array_elements_text(question_texts) val
         WHERE val IS NOT NULL AND val <> ''),
        '[]'::jsonb
    ) ||
    COALESCE(
        (SELECT jsonb_agg(jsonb_build_object('type', 'image', 'content', val))
         FROM jsonb_array_elements_text(question_image_paths) val
         WHERE val IS NOT NULL AND val <> ''),
        '[]'::jsonb
    )
);

UPDATE questions SET hint_blocks = (
    COALESCE(
        (SELECT jsonb_agg(jsonb_build_object('type', 'text', 'content', val))
         FROM jsonb_array_elements_text(hint_texts) val
         WHERE val IS NOT NULL AND val <> ''),
        '[]'::jsonb
    ) ||
    COALESCE(
        (SELECT jsonb_agg(jsonb_build_object('type', 'image', 'content', val))
         FROM jsonb_array_elements_text(hint_image_paths) val
         WHERE val IS NOT NULL AND val <> ''),
        '[]'::jsonb
    )
);

UPDATE questions SET explanation_blocks = (
    COALESCE(
        (SELECT jsonb_agg(jsonb_build_object('type', 'text', 'content', val))
         FROM jsonb_array_elements_text(explanation_texts) val
         WHERE val IS NOT NULL AND val <> ''),
        '[]'::jsonb
    ) ||
    COALESCE(
        (SELECT jsonb_agg(jsonb_build_object('type', 'image', 'content', val))
         FROM jsonb_array_elements_text(explanation_image_paths) val
         WHERE val IS NOT NULL AND val <> ''),
        '[]'::jsonb
    )
);

-- Drop the old separate columns
ALTER TABLE questions
    DROP COLUMN question_texts,
    DROP COLUMN question_image_paths,
    DROP COLUMN hint_texts,
    DROP COLUMN hint_image_paths,
    DROP COLUMN explanation_texts,
    DROP COLUMN explanation_image_paths;

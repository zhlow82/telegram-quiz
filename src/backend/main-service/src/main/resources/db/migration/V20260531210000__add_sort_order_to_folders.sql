ALTER TABLE folders ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;

UPDATE folders f
SET sort_order = sub.rn - 1
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY created_by ORDER BY created_at ASC) AS rn
    FROM folders
) sub
WHERE f.id = sub.id;

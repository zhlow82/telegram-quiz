ALTER TABLE questions ADD COLUMN updated_by VARCHAR(255);
UPDATE questions SET updated_by = created_by WHERE updated_by IS NULL;

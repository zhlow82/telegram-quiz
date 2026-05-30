ALTER TABLE users DROP COLUMN IF EXISTS display_name;
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name  VARCHAR(255);

-- Backfill local accounts: use username as both first and last name
UPDATE users SET first_name = username, last_name = username WHERE google_sub IS NULL;

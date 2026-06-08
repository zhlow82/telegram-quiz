ALTER TABLE questions
    ADD COLUMN IF NOT EXISTS show_briefing_primary_button BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS show_briefing_secondary_button BOOLEAN NOT NULL DEFAULT TRUE;
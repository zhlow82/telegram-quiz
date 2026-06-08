ALTER TABLE questions
    ADD COLUMN IF NOT EXISTS briefing_primary_button_text VARCHAR(255),
    ADD COLUMN IF NOT EXISTS briefing_secondary_button_text VARCHAR(255);

UPDATE questions
SET briefing_primary_button_text = COALESCE(NULLIF(TRIM(briefing_primary_button_text), ''), 'READY'),
    briefing_secondary_button_text = COALESCE(NULLIF(TRIM(briefing_secondary_button_text), ''), 'Start Timer')
WHERE is_briefing = TRUE;
ALTER TABLE reminders
    ADD is_scheduled BOOLEAN DEFAULT FALSE;

ALTER TABLE reminders
    ADD is_recurring BOOLEAN;

ALTER TABLE reminders
    ADD next_execution BIGINT;

ALTER TABLE reminders
    ADD zone_id VARCHAR(255);

ALTER TABLE reminders
    ALTER COLUMN is_scheduled SET NOT NULL;

ALTER TABLE reminders
    ALTER COLUMN is_recurring SET NOT NULL;

ALTER TABLE reminders
    ALTER COLUMN next_execution SET NOT NULL;

ALTER TABLE reminders
    ALTER COLUMN zone_id SET NOT NULL;
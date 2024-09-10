ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS endret_enhet BOOLEAN;

UPDATE bruker
SET endret_enhet = false;

ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS enhet TEXT;
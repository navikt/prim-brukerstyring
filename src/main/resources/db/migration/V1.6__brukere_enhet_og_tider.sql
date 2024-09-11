ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS endret_enhet BOOLEAN;

ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS enhet TEXT;

ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS sist_endret TIMESTAMP;

ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS created TIMESTAMP;

ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS updated TIMESTAMP;

UPDATE bruker
SET endret_enhet = false;

UPDATE bruker
SET sist_endret = sist_aksessert;

UPDATE bruker
SET created = sist_aksessert;

UPDATE bruker
SET updated = sist_aksessert;

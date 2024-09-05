ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS sluttet BOOLEAN;

UPDATE bruker
SET sluttet = false;
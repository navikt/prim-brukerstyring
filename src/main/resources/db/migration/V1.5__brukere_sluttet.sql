ALTER TABLE leder
    ADD COLUMN IF NOT EXISTS sluttet BOOLEAN;

UPDATE leder
SET sluttet = false;
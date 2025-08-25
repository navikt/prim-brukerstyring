ALTER TABLE bruker
    ADD COLUMN IF NOT EXISTS ukjent_tilgang TEXT;

UPDATE bruker
SET ukjent_tilgang = '';
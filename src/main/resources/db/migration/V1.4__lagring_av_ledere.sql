ALTER TABLE bruker
DROP COLUMN IF EXISTS representert_leder_id;

CREATE TABLE bruker_leder (
    ident      TEXT     NOT NULL,
    leder_id   BIGINT,
    CONSTRAINT FK_bruker_ident FOREIGN KEY (ident) REFERENCES bruker (ident),
    CONSTRAINT FK_leder_id FOREIGN KEY (leder_id) REFERENCES leder (leder_id)
);

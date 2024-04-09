CREATE SEQUENCE brukerstyring_sequence START 1000 INCREMENT 1;

CREATE TABLE leder (
    leder_id    BIGINT  NOT NULL,
    ident       TEXT    NOT NULL,
    navn        TEXT    NOT NULL,
    CONSTRAINT PK_leder PRIMARY KEY (leder_id)
);

ALTER TABLE bruker ADD COLUMN representert_leder_id TEXT;
ALTER TABLE bruker ADD CONSTRAINT FK_representert_leder_id FOREIGN KEY (representert_leder_id) REFERENCES leder (leder_id);
ALTER TABLE bruker ADD COLUMN sist_aksessert TIMESTAMP;

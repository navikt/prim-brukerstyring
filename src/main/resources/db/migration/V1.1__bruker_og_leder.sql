CREATE SEQUENCE brukerstyring_sequence START 1000 INCREMENT 1;

CREATE TABLE leder (
    leder_id                BIGINT  NOT NULL,
    ident                   TEXT    NOT NULL,
    navn                    TEXT    NOT NULL,
    er_direktoratsleder     BOOLEAN,
    CONSTRAINT PK_leder PRIMARY KEY (leder_id)
);

CREATE TABLE bruker (
    ident                   TEXT        NOT NULL,
    rolle                   TEXT        NOT NULL,
    navn                    TEXT,
    tilganger               TEXT,
    representert_leder_id   BIGINT,
    sist_aksessert          TIMESTAMP,
    CONSTRAINT PK_bruker PRIMARY KEY (ident),
    CONSTRAINT FK_representert_leder_id FOREIGN KEY (representert_leder_id) REFERENCES leder (leder_id)
);

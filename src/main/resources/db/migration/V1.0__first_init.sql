CREATE SEQUENCE brukerstyring_sequence START 1000 INCREMENT 1;

CREATE TABLE drift_og_vedlikehold (
    drift_og_vedlikehold_id BIGINT NOT NULL,
    vedlikehold_modus BOOLEAN NOT NULL,
    vedlikehold_overskrift TEXT NOT NULL,
    vedlikehold_melding TEXT NOT NULL,
    driftsmelding TEXT NOT NULL
);

INSERT INTO drift_og_vedlikehold (drift_og_vedlikehold_id, vedlikehold_modus, vedlikehold_overskrift, vedlikehold_melding, driftsmelding) VALUES (0, false, '', '', '');

CREATE TABLE leder (
    leder_id                BIGINT  NOT NULL,
    ident                   TEXT    NOT NULL,
    navn                    TEXT    NOT NULL,
    erDirektoratsleder      BOOLEAN,
    CONSTRAINT PK_leder PRIMARY KEY (leder_id)
);

CREATE TABLE bruker (
    ident TEXT NOT NULL,
    rolle TEXT NOT NULL,
    navn TEXT,
    tilganger TEXT,
    representert_leder_id BIGINT,
    sist_aksessert TIMESTAMP,
    CONSTRAINT PK_bruker PRIMARY KEY (ident),
    CONSTRAINT FK_representert_leder_id FOREIGN KEY (representert_leder_id) REFERENCES leder (leder_id)
);

CREATE TABLE ansatt (
    ansatt_id   BIGINT  NOT NULL,
    ident       TEXT    NOT NULL,
    navn        TEXT    NOT NULL,
    CONSTRAINT PK_ansatt PRIMARY KEY (ansatt_id)
);

CREATE TABLE ansatt_stillingsavtale (
    ansatt_id         BIGINT  NOT NULL,
    leder_id          BIGINT  NOT NULL,
    ansatt_type       TEXT    NOT NULL,
    stillingsavtale   TEXT    NOT NULL,
    CONSTRAINT PK_ansatt_stillingsavtale PRIMARY KEY (ansatt_id, leder_id),
    CONSTRAINT FK_ansatt FOREIGN KEY (ansatt_id) REFERENCES ansatt (ansatt_id),
    CONSTRAINT FK_leder FOREIGN KEY (leder_id) REFERENCES leder (leder_id)
);
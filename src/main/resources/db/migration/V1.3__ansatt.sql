CREATE TABLE ansatt (
    ansatt_id   BIGINT  NOT NULL,
    ident       TEXT    NOT NULL,
    navn        TEXT    NOT NULL,
    CONSTRAINT PK_ansatt PRIMARY KEY (ansatt_id)
);

CREATE TABLE ansatt_stillingsavtale
(
    ansatt_id         BIGINT  NOT NULL,
    leder_id          BIGINT  NOT NULL,
    ansatt_type       TEXT    NOT NULL,
    stillingsavtale   TEXT    NOT NULL,
    CONSTRAINT PK_ansatt_stillingsavtale PRIMARY KEY (ansatt_id, leder_id),
    CONSTRAINT FK_ansatt FOREIGN KEY (ansatt_id) REFERENCES ansatt (ansatt_id),
    CONSTRAINT FK_leder FOREIGN KEY (leder_id) REFERENCES leder (leder_id)
);

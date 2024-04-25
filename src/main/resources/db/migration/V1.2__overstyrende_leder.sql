CREATE TABLE overstyrende_leder (
    id                      BIGINT  NOT NULL,
    ansatt_ident            TEXT    NOT NULL,
    ansatt_navn             TEXT    NOT NULL,
    overstyrende_leder_id   BIGINT  NOT NULL,
    fra                     TIMESTAMP,
    til                     TIMESTAMP,
    CONSTRAINT PK_overstyrende_leder PRIMARY KEY (ansatt_ident),
    CONSTRAINT FK_overstyrende_leder_id FOREIGN KEY (overstyrende_leder_id) REFERENCES leder (leder_id)
);
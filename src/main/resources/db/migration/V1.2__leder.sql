CREATE TABLE leder (
    ident TEXT NOT NULL,
    navn TEXT
);

ALTER TABLE bruker ADD COLUMN representert_leder_ident TEXT;
ALTER TABLE bruker ADD CONSTRAINT FK_representert_leder_ident FOREIGN KEY (representert_leder_ident) REFERENCES leder (ident);
ALTER TABLE bruker ADD COLUMN sist_aksessert TIMESTAMP;

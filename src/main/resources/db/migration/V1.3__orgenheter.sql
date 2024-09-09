CREATE TABLE orgenhet (
   nom_id VARCHAR(255) NOT NULL,
   navn VARCHAR(255) NOT NULL,
   leder_leder_id BIGINT NOT NULL,
   CONSTRAINT FK_orgenhet_leder FOREIGN KEY (leder_leder_id) REFERENCES leder (leder_id)
);
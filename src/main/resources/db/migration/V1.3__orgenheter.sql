CREATE TABLE orgenhet (
   nom_id VARCHAR(255) NOT NULL,
   navn VARCHAR(255) NOT NULL,
   leder_id BIGINT NOT NULL,
   ADD CONSTRAINT FK_leder FOREIGN KEY (leder_id) REFERENCES leder (id);
);
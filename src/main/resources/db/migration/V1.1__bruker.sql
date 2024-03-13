CREATE TABLE bruker (
    ident TEXT NOT NULL,
    rolle TEXT NOT NULL,
    navn TEXT,
    tilganger TEXT,
    CONSTRAINT PK_bruker PRIMARY KEY (ident)
);
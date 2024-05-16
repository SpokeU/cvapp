CREATE TABLE IF NOT EXISTS resume
(
    id                   serial UNIQUE PRIMARY KEY,
    name                 varchar,
    profession           varchar,
    experience           integer,
    skills               varchar,
    highlights           varchar,
    resume_file_location varchar
);
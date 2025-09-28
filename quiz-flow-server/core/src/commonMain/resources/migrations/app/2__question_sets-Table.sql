CREATE TABLE question_sets (
    primary_key INTEGER ,
    id VARCHAR(64) NOT NULL,
    latest_version INTEGER NOT NULL,
    created_at TIMESTAMP,
    archived_at TIMESTAMP,
    json TEXT NOT NULL,

    PRIMARY KEY(primary_key)
);

CREATE UNIQUE INDEX uidx_question_sets_id ON question_sets(id);

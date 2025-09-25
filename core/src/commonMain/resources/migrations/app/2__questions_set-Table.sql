CREATE TABLE questions_set (
    primary_key INTEGER PRIMARY KEY,
    id VARCHAR(64) NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMP,
    archived_at TIMESTAMP,
    json TEXT NOT NULL
);

CREATE UNIQUE INDEX uidx_questions_set_id_version ON questions_set(id, version);

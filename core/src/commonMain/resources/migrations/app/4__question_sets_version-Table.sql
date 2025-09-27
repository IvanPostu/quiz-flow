CREATE TABLE question_set_versions (
    id VARCHAR(64),
    version INTEGER,
    created_at TIMESTAMP,
    json TEXT NOT NULL,

    PRIMARY KEY (id, version)
);

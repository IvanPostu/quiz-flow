CREATE TABLE quizzes (
    primary_key INTEGER ,
    id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    question_set_id VARCHAR(64) NOT NULL,
    question_set_version INTEGER NOT NULL,
    created_at TIMESTAMP,
    finalized_at TIMESTAMP,
    json TEXT NOT NULL,

    PRIMARY KEY(primary_key)
);

CREATE UNIQUE INDEX uidx_quizzes_id ON quizzes(id);
CREATE INDEX idx_quizzes_question_set_id_question_set_version ON quizzes(question_set_id, question_set_version);
CREATE INDEX idx_quizzes_user_id ON quizzes(user_id);

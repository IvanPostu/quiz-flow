CREATE TABLE users (
    primary_key INTEGER PRIMARY KEY,
    id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP,
    archived_at TIMESTAMP,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    json TEXT NOT NULL
);

CREATE UNIQUE INDEX uidx_users_id ON users(id);
CREATE UNIQUE INDEX uidx_users_username ON users(username);

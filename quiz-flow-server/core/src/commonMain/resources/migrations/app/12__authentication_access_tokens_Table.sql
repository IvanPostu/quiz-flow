CREATE TABLE authentication_access_tokens (
    primary_key INTEGER,
    id VARCHAR(64) NOT NULL,
    authentication_refresh_token_id VARCHAR(64) NOT NULL,
    access_token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,

    PRIMARY KEY(primary_key)
);

CREATE UNIQUE INDEX uidx_authentication_access_tokens_id
ON authentication_access_tokens(id);

CREATE UNIQUE INDEX uidx_authentication_access_tokens_access_token_hash
ON authentication_access_tokens(access_token_hash);

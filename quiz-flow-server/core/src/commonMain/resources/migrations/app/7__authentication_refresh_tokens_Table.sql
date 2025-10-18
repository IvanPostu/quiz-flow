CREATE TABLE authentication_refresh_tokens (
    primary_key INTEGER,
    id VARCHAR(64) NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    user_id VARCHAR(64) NOT NULL,

    PRIMARY KEY(primary_key)
);

CREATE UNIQUE INDEX uidx_authentication_refresh_tokens_id ON authentication_refresh_tokens(id);
CREATE UNIQUE INDEX uidx_authentication_refresh_tokens_refresh_token_hash
    ON authentication_refresh_tokens(refresh_token_hash);
CREATE INDEX uidx_authentication_refresh_tokens_user_id ON authentication_refresh_tokens(user_id);
CREATE INDEX uidx_authentication_refresh_tokens_expires_at ON authentication_refresh_tokens(expires_at);

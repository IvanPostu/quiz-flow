CREATE TABLE authorizations (
    primary_key INTEGER,
    access_token VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    impersonate_origin_user_id VARCHAR(64),

    PRIMARY KEY(primary_key)
);

CREATE UNIQUE INDEX uidx_authorizations_access_token ON authorizations(access_token);
CREATE UNIQUE INDEX uidx_authorizations_user_id ON authorizations(user_id);

CREATE TABLE authorization_scopes (
    primary_key INTEGER NOT NULL,
    scope VARCHAR(128) NOT NULL,
    PRIMARY KEY(primary_key)
);

CREATE UNIQUE INDEX uidx_authorization_scopes_scope ON authorization_scopes(scope);

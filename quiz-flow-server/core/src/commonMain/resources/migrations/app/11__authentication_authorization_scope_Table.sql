CREATE TABLE authentication_authorization_scope (
    authentication_primary_key INTEGER,
    authorization_scope_primary_key INTEGER,
    PRIMARY KEY(authentication_primary_key, authorization_scope_primary_key)
);

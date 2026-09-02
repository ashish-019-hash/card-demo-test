CREATE TABLE users (
    user_id VARCHAR(64) CONSTRAINT users_pkey PRIMARY KEY,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'REGULAR')),
    version BIGINT NOT NULL DEFAULT 0
);

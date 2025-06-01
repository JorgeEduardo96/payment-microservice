CREATE TABLE client (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

--rollback DROP TABLE client;

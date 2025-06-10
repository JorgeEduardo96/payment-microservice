CREATE TABLE client
(
    id         CHAR(36) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_client_email ON client (email);

ALTER TABLE client
    ADD CONSTRAINT uq_client_email UNIQUE (email);

--rollback DROP TABLE client;
--rollback DROP INDEX idx_client_email;
--rollback ALTER TABLE client DROP CONSTRAINT uq_client_email;

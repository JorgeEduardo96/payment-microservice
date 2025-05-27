CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_client_email ON client(email);
CREATE INDEX idx_client_cpf ON client(cpf);

ALTER TABLE client ADD CONSTRAINT uq_client_email UNIQUE (email);
ALTER TABLE client ADD CONSTRAINT uq_client_cpf UNIQUE (cpf);

--rollback DROP TABLE client;
--rollback DROP INDEX idx_client_email;
--rollback DROP INDEX idx_client_cpf;
--rollback ALTER TABLE client DROP CONSTRAINT uq_client_email;
--rollback ALTER TABLE client DROP CONSTRAINT uq_client_cpf;

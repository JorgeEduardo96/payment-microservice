CREATE TABLE order_saga (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    last_error VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_saga_order FOREIGN KEY (order_id) REFERENCES order_tb(id)
);

--rollback DROP TABLE order_saga;

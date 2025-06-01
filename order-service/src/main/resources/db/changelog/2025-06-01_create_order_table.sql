CREATE TABLE order_tb (
    id UUID PRIMARY KEY,
    total DECIMAL(19,2) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    payment_method VARCHAR(50),
    status VARCHAR(50),
    notes VARCHAR(500),
    created_at TIMESTAMP,
    client_id UUID NOT NULL,
    CONSTRAINT fk_order_client FOREIGN KEY (client_id) REFERENCES client(id)
);

--rollback DROP TABLE order;

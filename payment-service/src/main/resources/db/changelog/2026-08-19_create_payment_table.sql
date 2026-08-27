CREATE TABLE payment (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    client_id UUID NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_order_id ON payment(order_id);

-- order_id is the idempotency key: a payment for a given order must only ever be
-- decided/published once, even if the gRPC call is retried by order-service.
ALTER TABLE payment ADD CONSTRAINT uq_payment_order_id UNIQUE (order_id);

--rollback DROP TABLE payment;
--rollback DROP INDEX idx_payment_order_id;
--rollback ALTER TABLE payment DROP CONSTRAINT uq_payment_order_id;

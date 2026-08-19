CREATE TABLE outbox (
    id CHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id CHAR(36) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP DEFAULT NULL
);

CREATE INDEX idx_outbox_status_created_at ON outbox (status, created_at);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    turns INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    transaction_no VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);


CREATE INDEX idx_payment_user_id
    ON payments(user_id);

CREATE INDEX idx_payment_order_id
    ON payments(order_id);

CREATE INDEX idx_payment_status
    ON payments(status);
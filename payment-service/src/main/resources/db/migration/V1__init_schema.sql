-- Create payment_transactions table
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    customer_id VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_description VARCHAR(1000),
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(100),
    error_message VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for payment_transactions
CREATE UNIQUE INDEX idx_transaction_id ON payment_transactions(transaction_id);
CREATE INDEX idx_customer_id ON payment_transactions(customer_id);
CREATE INDEX idx_merchant_id ON payment_transactions(merchant_id);
CREATE INDEX idx_status ON payment_transactions(status);

-- Create saga_steps table
CREATE TABLE saga_steps (
    id UUID PRIMARY KEY,
    payment_transaction_id UUID NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempt INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_transaction 
        FOREIGN KEY (payment_transaction_id) 
        REFERENCES payment_transactions(id)
        ON DELETE CASCADE
);

-- Create indexes for saga_steps
CREATE INDEX idx_payment_transaction ON saga_steps(payment_transaction_id);
CREATE INDEX idx_step_status ON saga_steps(status);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    payment_transaction_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50),
    details VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for audit_logs
CREATE INDEX idx_payment_transaction_id ON audit_logs(payment_transaction_id);
CREATE INDEX idx_created_at ON audit_logs(created_at);

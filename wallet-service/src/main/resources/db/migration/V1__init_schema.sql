-- Create customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create wallets table
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    account_number VARCHAR(50) NOT NULL UNIQUE,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL CHECK (currency IN ('USD', 'EUR', 'INR')),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create wallet_reservations table
CREATE TABLE wallet_reservations (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    transaction_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    old_balance NUMERIC(19, 4),
    new_balance NUMERIC(19, 4),
    amount NUMERIC(19, 4) NOT NULL,
    transaction_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_wallets_customer_id ON wallets(customer_id);
CREATE INDEX idx_wallets_account_number ON wallets(account_number);
CREATE INDEX idx_reservations_wallet_id ON wallet_reservations(wallet_id);
CREATE INDEX idx_reservations_transaction_id ON wallet_reservations(transaction_id);
CREATE INDEX idx_reservations_status ON wallet_reservations(status);
CREATE INDEX idx_audit_logs_wallet_id ON audit_logs(wallet_id);
CREATE INDEX idx_audit_logs_transaction_id ON audit_logs(transaction_id);

-- Insert sample customers
INSERT INTO customers (id, name, email, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'John Doe', 'john.doe@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'Jane Smith', 'jane.smith@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'Bob Johnson', 'bob.johnson@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample wallets with balances
INSERT INTO wallets (id, customer_id, account_number, balance, currency, version, created_at, updated_at) VALUES
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'WAL-USD-1001', 1000.0000, 'USD', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'WAL-EUR-1002', 800.0000, 'EUR', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'WAL-INR-1003', 50000.0000, 'INR', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

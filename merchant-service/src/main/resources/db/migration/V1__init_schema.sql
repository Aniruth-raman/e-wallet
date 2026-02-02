-- Create merchants table
CREATE TABLE merchants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    business_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create merchant_wallets table
CREATE TABLE merchant_wallets (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

-- Create merchant_transactions table
CREATE TABLE merchant_transactions (
    id UUID PRIMARY KEY,
    merchant_wallet_id UUID NOT NULL,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_merchant_wallet FOREIGN KEY (merchant_wallet_id) REFERENCES merchant_wallets(id)
);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    merchant_wallet_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_balance DECIMAL(19, 2),
    new_balance DECIMAL(19, 2),
    amount DECIMAL(19, 2),
    transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

-- Create indexes
CREATE INDEX idx_merchant_email ON merchants(email);
CREATE INDEX idx_merchant_wallet_merchant_id ON merchant_wallets(merchant_id);
CREATE INDEX idx_merchant_wallet_currency ON merchant_wallets(merchant_id, currency);
CREATE INDEX idx_merchant_transaction_transaction_id ON merchant_transactions(transaction_id);
CREATE INDEX idx_merchant_transaction_wallet_id ON merchant_transactions(merchant_wallet_id);
CREATE INDEX idx_audit_log_wallet_id ON audit_logs(merchant_wallet_id);
CREATE INDEX idx_audit_log_transaction_id ON audit_logs(transaction_id);

-- Insert sample merchants
INSERT INTO merchants (id, name, email, business_type, created_at, updated_at)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'Electronics Store', 'electronics@example.com', 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'Bookstore', 'bookstore@example.com', 'Books', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'Grocery Shop', 'grocery@example.com', 'Grocery', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample merchant wallets
INSERT INTO merchant_wallets (id, merchant_id, account_number, balance, currency, version, created_at, updated_at)
VALUES 
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'MWLT-ELEC-USD-001', 0.00, 'USD', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'MWLT-BOOK-EUR-002', 0.00, 'EUR', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'MWLT-GROC-INR-003', 0.00, 'INR', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

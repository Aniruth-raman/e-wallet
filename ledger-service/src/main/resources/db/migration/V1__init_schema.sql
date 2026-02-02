-- Create ledger_entries table
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    customer_id VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255),
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    customer_balance_before DECIMAL(19, 2),
    customer_balance_after DECIMAL(19, 2),
    merchant_balance_before DECIMAL(19, 2),
    merchant_balance_after DECIMAL(19, 2),
    product_name VARCHAR(255),
    product_description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('PAYMENT', 'FEE', 'REFUND')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'COMPLETED', 'REVERSED')),
    CONSTRAINT chk_amount CHECK (amount >= 0)
);

-- Create indexes for performance
CREATE INDEX idx_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_customer_id ON ledger_entries(customer_id);
CREATE INDEX idx_merchant_id ON ledger_entries(merchant_id);
CREATE INDEX idx_created_at ON ledger_entries(created_at);
CREATE INDEX idx_status ON ledger_entries(status);
CREATE INDEX idx_transaction_type ON ledger_entries(transaction_type);

-- Create composite indexes for common queries
CREATE INDEX idx_customer_created ON ledger_entries(customer_id, created_at DESC);
CREATE INDEX idx_merchant_created ON ledger_entries(merchant_id, created_at DESC);

-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    ledger_entry_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ledger_entry FOREIGN KEY (ledger_entry_id) 
        REFERENCES ledger_entries(id) ON DELETE CASCADE
);

-- Create indexes for audit_logs
CREATE INDEX idx_ledger_entry_id ON audit_logs(ledger_entry_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);

-- Add comments for documentation
COMMENT ON TABLE ledger_entries IS 'Stores all financial transactions in the e-wallet system';
COMMENT ON TABLE audit_logs IS 'Audit trail for all ledger entry operations';
COMMENT ON COLUMN ledger_entries.transaction_id IS 'Unique transaction identifier for idempotency';
COMMENT ON COLUMN ledger_entries.status IS 'Current status of the transaction';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed on the ledger entry';

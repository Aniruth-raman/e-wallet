-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    recipient_id VARCHAR(255) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    transaction_id VARCHAR(255),
    amount DECIMAL(19, 2),
    currency VARCHAR(3),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for notifications table
CREATE INDEX IF NOT EXISTS idx_recipient_id ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_transaction_id ON notifications(transaction_id);
CREATE INDEX IF NOT EXISTS idx_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON notifications(created_at);

-- Create indexes for audit_logs table
CREATE INDEX IF NOT EXISTS idx_notification_id ON audit_logs(notification_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);

-- Add foreign key constraint
ALTER TABLE audit_logs 
ADD CONSTRAINT fk_audit_notification 
FOREIGN KEY (notification_id) 
REFERENCES notifications(id) 
ON DELETE CASCADE;

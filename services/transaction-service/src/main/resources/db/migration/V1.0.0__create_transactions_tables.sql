-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL UNIQUE,
    payment_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);

-- Create transaction_entries table
CREATE TABLE IF NOT EXISTS transaction_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    user_id UUID NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    CONSTRAINT fk_transaction_entry_transaction 
        FOREIGN KEY (transaction_id) 
        REFERENCES transactions(id) 
        ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_transaction_payment_id ON transactions(payment_id);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_entry_transaction_id ON transaction_entries(transaction_id);
CREATE INDEX IF NOT EXISTS idx_entry_user_id ON transaction_entries(user_id);

-- Add check constraints
ALTER TABLE transactions ADD CONSTRAINT chk_transaction_status 
    CHECK (status IN ('PENDING', 'RECORDED', 'FAILED', 'REVERSED'));
ALTER TABLE transaction_entries ADD CONSTRAINT chk_entry_type 
    CHECK (entry_type IN ('DEBIT', 'CREDIT'));
ALTER TABLE transaction_entries ADD CONSTRAINT chk_amount_positive 
    CHECK (amount > 0);

-- Add comments to tables
COMMENT ON TABLE transactions IS 'Stores transaction records with double-entry bookkeeping';
COMMENT ON COLUMN transactions.id IS 'Primary key';
COMMENT ON COLUMN transactions.transaction_id IS 'Unique transaction identifier';
COMMENT ON COLUMN transactions.payment_id IS 'Reference to payment that triggered this transaction';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, RECORDED, FAILED, REVERSED';
COMMENT ON COLUMN transactions.version IS 'Optimistic locking version field';

COMMENT ON TABLE transaction_entries IS 'Stores individual entries for double-entry bookkeeping';
COMMENT ON COLUMN transaction_entries.id IS 'Primary key';
COMMENT ON COLUMN transaction_entries.transaction_id IS 'Reference to parent transaction';
COMMENT ON COLUMN transaction_entries.user_id IS 'User ID for this entry';
COMMENT ON COLUMN transaction_entries.entry_type IS 'Entry type: DEBIT or CREDIT';
COMMENT ON COLUMN transaction_entries.amount IS 'Entry amount (must be positive)';
COMMENT ON COLUMN transaction_entries.currency IS 'Currency code (ISO 4217)';





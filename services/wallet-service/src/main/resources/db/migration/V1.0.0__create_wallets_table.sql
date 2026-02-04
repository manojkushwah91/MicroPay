-- Create wallets table
CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create index on user_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_wallet_user_id ON wallets(user_id);

-- Create index on status for filtering
CREATE INDEX IF NOT EXISTS idx_wallet_status ON wallets(status);

-- Add check constraint for balance (cannot be negative)
ALTER TABLE wallets ADD CONSTRAINT chk_balance_non_negative CHECK (balance >= 0);

-- Add check constraint for status
ALTER TABLE wallets ADD CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'));

-- Add comment to table
COMMENT ON TABLE wallets IS 'Stores user wallet information including balance and status';
COMMENT ON COLUMN wallets.id IS 'Unique wallet identifier';
COMMENT ON COLUMN wallets.user_id IS 'Reference to user who owns this wallet';
COMMENT ON COLUMN wallets.balance IS 'Current wallet balance (non-negative)';
COMMENT ON COLUMN wallets.currency IS 'Currency code (ISO 4217)';
COMMENT ON COLUMN wallets.status IS 'Wallet status: ACTIVE, SUSPENDED, or CLOSED';
COMMENT ON COLUMN wallets.version IS 'Optimistic locking version field';








-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL UNIQUE,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    payer_user_id UUID NOT NULL,
    payee_user_id UUID,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    failure_reason VARCHAR(50),
    error_code VARCHAR(50),
    error_message VARCHAR(500),
    description VARCHAR(500),
    reference VARCHAR(100),
    transaction_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    authorized_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_payment_payer_id ON payments(payer_user_id);
CREATE INDEX IF NOT EXISTS idx_payment_payee_id ON payments(payee_user_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_idempotency ON payments(idempotency_key);

-- Add check constraints
ALTER TABLE payments ADD CONSTRAINT chk_amount_positive CHECK (amount > 0);
ALTER TABLE payments ADD CONSTRAINT chk_payment_status CHECK (status IN ('INITIATED', 'AUTHORIZED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED'));
ALTER TABLE payments ADD CONSTRAINT chk_payment_type CHECK (payment_type IN ('TRANSFER', 'PAYMENT', 'REFUND'));

-- Add comments to table
COMMENT ON TABLE payments IS 'Stores payment transaction information';
COMMENT ON COLUMN payments.id IS 'Primary key';
COMMENT ON COLUMN payments.payment_id IS 'Unique payment identifier (for idempotency)';
COMMENT ON COLUMN payments.idempotency_key IS 'Idempotency key to prevent duplicate payments';
COMMENT ON COLUMN payments.payer_user_id IS 'User ID of the payer';
COMMENT ON COLUMN payments.payee_user_id IS 'User ID of the payee (optional)';
COMMENT ON COLUMN payments.amount IS 'Payment amount (must be positive)';
COMMENT ON COLUMN payments.currency IS 'Currency code (ISO 4217)';
COMMENT ON COLUMN payments.payment_type IS 'Type of payment: TRANSFER, PAYMENT, or REFUND';
COMMENT ON COLUMN payments.status IS 'Payment status: INITIATED, AUTHORIZED, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED';
COMMENT ON COLUMN payments.version IS 'Optimistic locking version field';








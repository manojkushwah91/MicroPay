-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notifications(created_at);

-- Add check constraints
ALTER TABLE notifications ADD CONSTRAINT chk_notification_status 
    CHECK (status IN ('PENDING', 'SENT', 'FAILED'));
ALTER TABLE notifications ADD CONSTRAINT chk_notification_channel 
    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'));
ALTER TABLE notifications ADD CONSTRAINT chk_notification_type 
    CHECK (notification_type IN ('PAYMENT_COMPLETED', 'TRANSACTION_RECORDED', 'PAYMENT_FAILED', 
                                 'WALLET_BALANCE_UPDATED', 'ACCOUNT_VERIFIED', 'SECURITY_ALERT'));

-- Add comments to table
COMMENT ON TABLE notifications IS 'Stores notification records for users';
COMMENT ON COLUMN notifications.id IS 'Primary key';
COMMENT ON COLUMN notifications.user_id IS 'User ID who receives the notification';
COMMENT ON COLUMN notifications.notification_type IS 'Type of notification';
COMMENT ON COLUMN notifications.channel IS 'Notification channel: EMAIL, SMS, PUSH, IN_APP';
COMMENT ON COLUMN notifications.status IS 'Notification status: PENDING, SENT, FAILED';
COMMENT ON COLUMN notifications.title IS 'Notification title';
COMMENT ON COLUMN notifications.message IS 'Notification message content';
COMMENT ON COLUMN notifications.reference_id IS 'Reference to related entity (paymentId, transactionId, etc.)';
COMMENT ON COLUMN notifications.reference_type IS 'Type of reference: PAYMENT, TRANSACTION, etc.';
COMMENT ON COLUMN notifications.version IS 'Optimistic locking version field';








# Database Schema Design (Database-per-Service)

## Schema Ownership Principle

Each microservice owns its database. Services cannot directly access other services' databases. Data sharing occurs only through:
1. **API calls** (synchronous)
2. **Kafka events** (asynchronous)
3. **Shared read replicas** (for reporting, read-only)

## Database Naming Convention

Format: `micropay_{service_name}_db`

Examples:
- `micropay_user_db`
- `micropay_payment_db`
- `micropay_transaction_db`

---

## 1. User Service Database

**Database**: `micropay_user_db`  
**Schema**: `user_schema`

### Tables

#### users
```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    kyc_status VARCHAR(50) DEFAULT 'PENDING',
    kyc_verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    deleted_at TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone_number),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

#### user_profiles
```sql
CREATE TABLE user_profiles (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    date_of_birth DATE,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    preferred_currency VARCHAR(3) DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);
```

#### user_preferences
```sql
CREATE TABLE user_preferences (
    preference_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    email_notifications_enabled BOOLEAN DEFAULT TRUE,
    sms_notifications_enabled BOOLEAN DEFAULT TRUE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,
    marketing_emails_enabled BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);
```

#### user_sessions
```sql
CREATE TABLE user_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    device_info JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
);
```

#### user_activity_logs
```sql
CREATE TABLE user_activity_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    activity_type VARCHAR(100) NOT NULL,
    activity_description TEXT,
    metadata JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at)
);
```

---

## 2. Account Service Database

**Database**: `micropay_account_db`  
**Schema**: `account_schema`

### Tables

#### accounts
```sql
CREATE TABLE accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_type VARCHAR(50) NOT NULL DEFAULT 'PRIMARY',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    balance_snapshot DECIMAL(19, 4) DEFAULT 0.0000,
    balance_last_updated_at TIMESTAMP,
    daily_transfer_limit DECIMAL(19, 4),
    monthly_transfer_limit DECIMAL(19, 4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_account_number (account_number),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

**Note**: `balance_snapshot` is eventually consistent. Real-time balance calculated by Balance Service.

#### account_limits
```sql
CREATE TABLE account_limits (
    limit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    limit_type VARCHAR(50) NOT NULL,
    limit_amount DECIMAL(19, 4) NOT NULL,
    period_type VARCHAR(50) NOT NULL,
    current_usage DECIMAL(19, 4) DEFAULT 0.0000,
    period_start_at TIMESTAMP NOT NULL,
    period_end_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_limit_type (limit_type),
    INDEX idx_period_end_at (period_end_at)
);
```

#### account_statements
```sql
CREATE TABLE account_statements (
    statement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    statement_period_start TIMESTAMP NOT NULL,
    statement_period_end TIMESTAMP NOT NULL,
    opening_balance DECIMAL(19, 4) NOT NULL,
    closing_balance DECIMAL(19, 4) NOT NULL,
    total_debits DECIMAL(19, 4) DEFAULT 0.0000,
    total_credits DECIMAL(19, 4) DEFAULT 0.0000,
    transaction_count INTEGER DEFAULT 0,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(500),
    INDEX idx_account_id (account_id),
    INDEX idx_period_end (statement_period_end)
);
```

---

## 3. Payment Service Database

**Database**: `micropay_payment_db`  
**Schema**: `payment_schema`

### Tables

#### payments
```sql
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    payer_account_id UUID NOT NULL,
    payee_account_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_type VARCHAR(50) NOT NULL DEFAULT 'TRANSFER',
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    description TEXT,
    reference VARCHAR(255),
    initiated_by UUID NOT NULL,
    failure_reason VARCHAR(255),
    error_code VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    INDEX idx_payer_account (payer_account_id),
    INDEX idx_payee_account (payee_account_id),
    INDEX idx_status (status),
    INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_created_at (created_at),
    INDEX idx_completed_at (completed_at)
);
```

#### payment_metadata
```sql
CREATE TABLE payment_metadata (
    metadata_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(payment_id) ON DELETE CASCADE,
    metadata_key VARCHAR(100) NOT NULL,
    metadata_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(payment_id, metadata_key),
    INDEX idx_payment_id (payment_id)
);
```

#### payment_retries
```sql
CREATE TABLE payment_retries (
    retry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(payment_id) ON DELETE CASCADE,
    retry_attempt INTEGER NOT NULL,
    retry_reason VARCHAR(255),
    retry_status VARCHAR(50) NOT NULL,
    error_message TEXT,
    retried_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_id (payment_id),
    INDEX idx_retry_status (retry_status)
);
```

---

## 4. Transaction Service Database

**Database**: `micropay_transaction_db`  
**Schema**: `transaction_schema`

### Tables

#### transactions
```sql
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP,
    reversed_at TIMESTAMP,
    reversal_reason VARCHAR(255),
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status),
    INDEX idx_settled_at (settled_at),
    INDEX idx_created_at (created_at)
);
```

#### transaction_entries
```sql
CREATE TABLE transaction_entries (
    entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    account_id UUID NOT NULL,
    entry_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance_before DECIMAL(19, 4),
    balance_after DECIMAL(19, 4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_account_id (account_id),
    INDEX idx_entry_type (entry_type)
);
```

**Business Rule**: Every transaction must have at least one DEBIT and one CREDIT entry. Sum of debits = Sum of credits.

#### transaction_reversals
```sql
CREATE TABLE transaction_reversals (
    reversal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_transaction_id UUID NOT NULL REFERENCES transactions(transaction_id),
    reversal_transaction_id UUID NOT NULL REFERENCES transactions(transaction_id),
    reversal_reason VARCHAR(255) NOT NULL,
    reversed_by UUID,
    reversed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_original_transaction (original_transaction_id),
    INDEX idx_reversal_transaction (reversal_transaction_id)
);
```

---

## 5. Balance Service Database

**Database**: `micropay_balance_db`  
**Schema**: `balance_schema`

### Tables

#### balance_snapshots
```sql
CREATE TABLE balance_snapshots (
    snapshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    balance DECIMAL(19, 4) NOT NULL,
    available_balance DECIMAL(19, 4) NOT NULL,
    reserved_balance DECIMAL(19, 4) DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL,
    snapshot_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_event_id VARCHAR(255),
    INDEX idx_account_id (account_id),
    INDEX idx_snapshot_at (snapshot_at)
);
```

**Note**: Snapshots are created periodically (every 100 events or every 5 minutes) for performance.

#### balance_events
```sql
CREATE TABLE balance_events (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_source VARCHAR(100) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance_before DECIMAL(19, 4),
    balance_after DECIMAL(19, 4),
    transaction_id UUID,
    payment_id UUID,
    kafka_event_id VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_event_type (event_type),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_payment_id (payment_id)
);
```

**Architecture**: Event sourcing pattern. Balance calculated by replaying events.

#### balance_reservations
```sql
CREATE TABLE balance_reservations (
    reservation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    payment_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
);
```

---

## 6. Notification Service Database

**Database**: `micropay_notification_db`  
**Schema**: `notification_schema`

### Tables

#### notifications
```sql
CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_channel (channel)
);
```

#### notification_templates
```sql
CREATE TABLE notification_templates (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(100) UNIQUE NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject_template TEXT,
    body_template TEXT NOT NULL,
    variables JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_template_name (template_name),
    INDEX idx_template_type (template_type)
);
```

---

## 7. Fraud Detection Service Database

**Database**: `micropay_fraud_db`  
**Schema**: `fraud_schema`

### Tables

#### fraud_assessments
```sql
CREATE TABLE fraud_assessments (
    assessment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    user_id UUID,
    risk_score DECIMAL(5, 2) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    recommendation VARCHAR(50) NOT NULL,
    risk_factors JSONB,
    model_version VARCHAR(50),
    assessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_id (payment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_risk_level (risk_level),
    INDEX idx_assessed_at (assessed_at)
);
```

#### fraud_alerts
```sql
CREATE TABLE fraud_alerts (
    alert_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES fraud_assessments(assessment_id),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    resolved_at TIMESTAMP,
    resolved_by UUID,
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_assessment_id (assessment_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity)
);
```

#### fraud_rules
```sql
CREATE TABLE fraud_rules (
    rule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(100) UNIQUE NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    rule_condition JSONB NOT NULL,
    risk_score_impact DECIMAL(5, 2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rule_name (rule_name),
    INDEX idx_is_active (is_active)
);
```

---

## 8. Audit Service Database

**Database**: `micropay_audit_db`  
**Schema**: `audit_schema`

### Tables

#### audit_logs
```sql
CREATE TABLE audit_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(255),
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    metadata JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_service_name (service_name),
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_id (entity_id),
    INDEX idx_user_id (user_id),
    INDEX idx_occurred_at (occurred_at)
) PARTITION BY RANGE (occurred_at);
```

**Partitioning Strategy**: Monthly partitions for performance and archival.

#### audit_logs_archived
```sql
CREATE TABLE audit_logs_archived (
    -- Same structure as audit_logs
    -- Used for cold storage (older than 1 year)
) PARTITION BY RANGE (occurred_at);
```

---

## 9. Reporting Service Database

**Database**: `micropay_reporting_db`  
**Schema**: `reporting_schema`

### Tables

#### financial_reports
```sql
CREATE TABLE financial_reports (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(100) NOT NULL,
    report_period_start TIMESTAMP NOT NULL,
    report_period_end TIMESTAMP NOT NULL,
    account_id UUID,
    user_id UUID,
    total_revenue DECIMAL(19, 4),
    total_expenses DECIMAL(19, 4),
    transaction_count INTEGER,
    report_data JSONB NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by UUID,
    INDEX idx_report_type (report_type),
    INDEX idx_period_end (report_period_end),
    INDEX idx_account_id (account_id),
    INDEX idx_user_id (user_id)
);
```

#### report_cache
```sql
CREATE TABLE report_cache (
    cache_key VARCHAR(255) PRIMARY KEY,
    report_data JSONB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_expires_at (expires_at)
);
```

---

## Database Connection Strategy

### Connection Pooling
- **HikariCP** (Spring Boot default)
- **Pool Size**: 10-20 connections per service instance
- **Max Lifetime**: 30 minutes
- **Idle Timeout**: 10 minutes

### Read Replicas
- **Reporting Service**: Uses read replicas for analytics queries
- **Account Service**: Uses read replica for balance queries (eventual consistency acceptable)

### Database Migrations
- **Tool**: Flyway or Liquibase
- **Location**: Each service contains its own migration scripts
- **Versioning**: Semantic versioning (e.g., V1.0.0__create_users_table.sql)

### Backup Strategy
- **Full Backup**: Daily at 2 AM UTC
- **Incremental Backup**: Every 6 hours
- **Retention**: 30 days (hot), 1 year (cold)
- **Point-in-Time Recovery**: Enabled

### High Availability
- **Primary-Replica Setup**: 1 primary, 2 replicas per database
- **Failover**: Automatic failover (max 30 seconds)
- **Connection String**: Includes multiple hosts for failover


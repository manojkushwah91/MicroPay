# End-to-End Payment Event Flow

## Payment Flow Overview

MicroPay uses an **event-driven, choreography-based Saga pattern** for payment processing. All state changes flow through Kafka, ensuring eventual consistency and auditability.

## Flow Diagram (Text Representation)

```
Client Request
    ↓
API Gateway (JWT validation, rate limiting)
    ↓
Payment Service (POST /api/v1/payments)
    ↓
[1] Payment Service: Validate request, create payment record (status: INITIATED)
    ↓
[2] Kafka: Produce "payment.initiated" event
    ↓
    ├─→ Balance Service: Reserve balance
    ├─→ Fraud Detection Service: Assess risk
    └─→ Transaction Service: Create transaction entries
    ↓
[3] Balance Service: Check available balance
    ├─→ If insufficient: Produce "payment.failed" → End
    └─→ If sufficient: Reserve balance, produce "account.balance.reserved"
    ↓
[4] Fraud Detection Service: Assess payment risk
    ├─→ If HIGH/CRITICAL risk: Produce "fraud.alert.triggered", Payment Service blocks
    └─→ If LOW/MEDIUM: Produce "fraud.risk.assessed" (ALLOW)
    ↓
[5] Payment Service: Consumes "account.balance.reserved" and "fraud.risk.assessed"
    ├─→ If both successful: Update payment status to PROCESSING
    └─→ Produce "payment.processing"
    ↓
[6] Transaction Service: Consumes "payment.processing"
    ├─→ Create debit entry (payer account)
    ├─→ Create credit entry (payee account)
    └─→ Produce "transaction.created"
    ↓
[7] Transaction Service: Settle transaction (double-entry)
    └─→ Produce "transaction.settled"
    ↓
[8] Balance Service: Consumes "transaction.settled"
    ├─→ Update payer balance (debit)
    ├─→ Update payee balance (credit)
    └─→ Produce "account.balance.updated" (2 events: one per account)
    ↓
[9] Payment Service: Consumes "transaction.settled"
    ├─→ Update payment status to COMPLETED
    └─→ Produce "payment.completed"
    ↓
[10] Multiple Services Consume "payment.completed"
    ├─→ Account Service: Update account last activity
    ├─→ Notification Service: Send confirmation emails/SMS
    ├─→ User Service: Update user activity
    ├─→ Reporting Service: Update revenue metrics
    └─→ Audit Service: Log payment completion
    ↓
[11] WebSocket: Real-time notification to client
    └─→ Notification Service → WebSocket Gateway → Client
```

## Detailed Step-by-Step Flow

### Step 1: Payment Initiation

**Actor**: Client Application  
**Action**: POST `/api/v1/payments`

**Request**:
```json
{
  "payerAccountId": "uuid",
  "payeeAccountId": "uuid",
  "amount": 100.00,
  "currency": "USD",
  "description": "Payment for services",
  "idempotencyKey": "unique-key-12345"
}
```

**Payment Service Processing**:
1. Validate JWT token (from API Gateway)
2. Validate request (amount > 0, accounts exist, same currency)
3. Check idempotency key (Redis lookup)
   - If exists: Return existing payment
   - If new: Continue
4. Create payment record in database (status: `INITIATED`)
5. Store idempotency key in Redis (TTL: 24 hours)
6. Produce Kafka event: `payment.initiated`

**Kafka Event**:
```json
{
  "eventId": "evt-001",
  "eventType": "payment.initiated",
  "timestamp": "2024-01-15T10:00:00Z",
  "paymentId": "pay-123",
  "idempotencyKey": "unique-key-12345",
  "payerAccountId": "acc-001",
  "payeeAccountId": "acc-002",
  "amount": 100.00,
  "currency": "USD",
  "paymentType": "TRANSFER",
  "status": "INITIATED",
  "metadata": {
    "description": "Payment for services",
    "initiatedBy": "user-001"
  }
}
```

---

### Step 2: Parallel Processing (Balance Reservation & Fraud Check)

#### 2a. Balance Service: Reserve Balance

**Consumer**: Balance Service (consumes `payment.initiated`)

**Processing**:
1. Check current balance (from event-sourced balance or Redis cache)
2. Validate sufficient funds
3. If insufficient:
   - Produce `payment.failed` event
   - End flow
4. If sufficient:
   - Create balance reservation (hold)
   - Update balance (available = total - reserved)
   - Produce `account.balance.reserved` event

**Kafka Event**:
```json
{
  "eventId": "evt-002",
  "eventType": "account.balance.reserved",
  "timestamp": "2024-01-15T10:00:01Z",
  "accountId": "acc-001",
  "reservationId": "res-001",
  "amount": 100.00,
  "currency": "USD",
  "paymentId": "pay-123",
  "expiresAt": "2024-01-15T10:15:00Z",
  "metadata": {}
}
```

#### 2b. Fraud Detection Service: Risk Assessment

**Consumer**: Fraud Detection Service (consumes `payment.initiated`)

**Processing**:
1. Extract payment features (amount, accounts, user history, device, location)
2. Run fraud detection rules and ML model
3. Calculate risk score (0-100)
4. Determine risk level:
   - LOW (0-30): Allow
   - MEDIUM (31-60): Allow with review flag
   - HIGH (61-85): Block or require additional verification
   - CRITICAL (86-100): Block immediately
5. Produce `fraud.risk.assessed` event

**Kafka Event**:
```json
{
  "eventId": "evt-003",
  "eventType": "fraud.risk.assessed",
  "timestamp": "2024-01-15T10:00:02Z",
  "paymentId": "pay-123",
  "riskScore": 25.5,
  "riskLevel": "LOW",
  "riskFactors": [],
  "recommendation": "ALLOW",
  "metadata": {}
}
```

---

### Step 3: Payment Processing Decision

**Consumer**: Payment Service (consumes `account.balance.reserved` and `fraud.risk.assessed`)

**Processing**:
1. Wait for both events (or timeout after 5 seconds)
2. If balance reserved AND fraud check passed:
   - Update payment status to `PROCESSING`
   - Produce `payment.processing` event
3. If fraud check failed (HIGH/CRITICAL):
   - Release balance reservation
   - Update payment status to `FAILED`
   - Produce `payment.failed` event
   - End flow

**Kafka Event**:
```json
{
  "eventId": "evt-004",
  "eventType": "payment.processing",
  "timestamp": "2024-01-15T10:00:03Z",
  "paymentId": "pay-123",
  "payerAccountId": "acc-001",
  "payeeAccountId": "acc-002",
  "amount": 100.00,
  "currency": "USD",
  "status": "PROCESSING",
  "metadata": {}
}
```

---

### Step 4: Transaction Creation

**Consumer**: Transaction Service (consumes `payment.processing`)

**Processing**:
1. Create transaction record
2. Create debit entry:
   - Account: `acc-001` (payer)
   - Type: `DEBIT`
   - Amount: 100.00
3. Create credit entry:
   - Account: `acc-002` (payee)
   - Type: `CREDIT`
   - Amount: 100.00
4. Validate: Total debits = Total credits
5. Store transaction (status: `PENDING`)
6. Produce `transaction.created` event

**Kafka Event**:
```json
{
  "eventId": "evt-005",
  "eventType": "transaction.created",
  "timestamp": "2024-01-15T10:00:04Z",
  "transactionId": "txn-001",
  "paymentId": "pay-123",
  "entries": [
    {
      "accountId": "acc-001",
      "type": "DEBIT",
      "amount": 100.00,
      "currency": "USD"
    },
    {
      "accountId": "acc-002",
      "type": "CREDIT",
      "amount": 100.00,
      "currency": "USD"
    }
  ],
  "status": "PENDING",
  "metadata": {}
}
```

---

### Step 5: Transaction Settlement

**Consumer**: Transaction Service (internal processing after `transaction.created`)

**Processing**:
1. Validate all entries are valid
2. Mark transaction as `SETTLED`
3. Produce `transaction.settled` event

**Kafka Event**:
```json
{
  "eventId": "evt-006",
  "eventType": "transaction.settled",
  "timestamp": "2024-01-15T10:00:05Z",
  "transactionId": "txn-001",
  "paymentId": "pay-123",
  "entries": [
    {
      "accountId": "acc-001",
      "type": "DEBIT",
      "amount": 100.00,
      "currency": "USD"
    },
    {
      "accountId": "acc-002",
      "type": "CREDIT",
      "amount": 100.00,
      "currency": "USD"
    }
  ],
  "settledAt": "2024-01-15T10:00:05Z",
  "metadata": {}
}
```

---

### Step 6: Balance Update

**Consumer**: Balance Service (consumes `transaction.settled`)

**Processing**:
1. For each entry in transaction:
   - If DEBIT: Decrease account balance, release reservation
   - If CREDIT: Increase account balance
2. Update balance snapshots (event sourcing)
3. Update Redis cache
4. Produce `account.balance.updated` events (one per account)

**Kafka Events**:
```json
// Event for payer account
{
  "eventId": "evt-007",
  "eventType": "account.balance.updated",
  "timestamp": "2024-01-15T10:00:06Z",
  "accountId": "acc-001",
  "previousBalance": 500.00,
  "newBalance": 400.00,
  "changeAmount": -100.00,
  "transactionId": "txn-001",
  "metadata": {}
}

// Event for payee account
{
  "eventId": "evt-008",
  "eventType": "account.balance.updated",
  "timestamp": "2024-01-15T10:00:06Z",
  "accountId": "acc-002",
  "previousBalance": 200.00,
  "newBalance": 300.00,
  "changeAmount": 100.00,
  "transactionId": "txn-001",
  "metadata": {}
}
```

---

### Step 7: Payment Completion

**Consumer**: Payment Service (consumes `transaction.settled`)

**Processing**:
1. Update payment status to `COMPLETED`
2. Set `completedAt` timestamp
3. Produce `payment.completed` event

**Kafka Event**:
```json
{
  "eventId": "evt-009",
  "eventType": "payment.completed",
  "timestamp": "2024-01-15T10:00:07Z",
  "paymentId": "pay-123",
  "payerAccountId": "acc-001",
  "payeeAccountId": "acc-002",
  "amount": 100.00,
  "currency": "USD",
  "transactionId": "txn-001",
  "completedAt": "2024-01-15T10:00:07Z",
  "metadata": {}
}
```

---

### Step 8: Downstream Processing

Multiple services consume `payment.completed` in parallel:

#### 8a. Account Service
- Update account last activity timestamp
- Update account statistics

#### 8b. Notification Service
- Send email to payer: "Payment sent"
- Send email to payee: "Payment received"
- Send SMS notifications (if enabled)
- Send push notifications (if mobile app)

#### 8c. User Service
- Update user activity log
- Update user statistics

#### 8d. Reporting Service
- Update revenue metrics
- Update transaction volume
- Update daily/weekly/monthly reports

#### 8e. Audit Service
- Log payment completion for compliance

#### 8f. WebSocket Gateway
- Send real-time notification to connected clients
- Notify payer and payee about payment completion

---

## Failure Scenarios and Compensation

### Scenario 1: Insufficient Balance

**Flow**:
1. Payment initiated
2. Balance Service checks balance → Insufficient
3. Balance Service produces `payment.failed`
4. Payment Service updates status to `FAILED`
5. Notification Service sends failure notification

**Compensation**: None needed (no state changed)

---

### Scenario 2: Fraud Detected

**Flow**:
1. Payment initiated
2. Balance reserved
3. Fraud Detection: HIGH risk → Block
4. Payment Service: Release balance reservation
5. Balance Service: Produce `account.balance.released`
6. Payment Service: Update status to `FAILED`
7. Notification Service: Send fraud alert

**Compensation**: Balance reservation released

---

### Scenario 3: Transaction Service Failure

**Flow**:
1. Payment processing
2. Transaction Service fails to create transaction
3. Timeout (5 seconds)
4. Payment Service: Compensating action
   - Release balance reservation
   - Update payment status to `FAILED`
   - Produce `payment.failed`

**Compensation**: Balance reservation released

---

### Scenario 4: Partial Failure After Settlement

**Flow**:
1. Transaction settled
2. Balance update fails for one account
3. Retry mechanism:
   - Retry balance update (exponential backoff)
   - If still fails: Alert operations team
   - Manual reconciliation process

**Compensation**: Manual intervention required (rare)

---

## Idempotency Guarantees

### Payment Service
- Idempotency key stored in Redis (TTL: 24 hours)
- Duplicate requests with same key return existing payment

### Balance Service
- Event deduplication by `eventId`
- Idempotent balance updates (event sourcing)

### Transaction Service
- Transaction ID uniqueness
- Idempotent transaction creation

## Event Ordering

### Per-Payment Ordering
- All events for same `paymentId` processed in order
- Achieved by partitioning Kafka topics by `paymentId`

### Cross-Payment Ordering
- Not guaranteed (different partitions)
- Use event timestamps for ordering when needed

## Monitoring and Observability

### Key Metrics
- Payment initiation rate
- Payment success rate
- Payment failure rate (by reason)
- Average payment processing time
- Event processing lag (Kafka consumer lag)
- Balance reservation success rate
- Fraud detection false positive rate

### Alerts
- Payment failure rate > 5%
- Payment processing time > 10 seconds
- Kafka consumer lag > 1000 messages
- Balance reservation failures
- Transaction settlement failures


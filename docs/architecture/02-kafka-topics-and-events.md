# Kafka Topics and Event Design

## Topic Naming Convention

Format: `{domain}.{entity}.{action}`

Examples:
- `payment.initiated`
- `account.balance.updated`
- `user.created`

## Topic Configuration

### Retention Policy
- **State Events**: 7 days (hot), 90 days (warm), 1 year (cold storage)
- **Audit Events**: 1 year (hot), 7 years (cold storage)
- **Notification Events**: 24 hours

### Partitioning Strategy
- Partition by entity ID (e.g., `userId`, `accountId`, `paymentId`)
- Ensures ordering per entity
- Enables parallel processing

### Replication Factor
- **Production**: 3 replicas
- **Staging**: 2 replicas
- **Development**: 1 replica

## Core Domain Topics

### 1. User Domain

#### Topic: `user.created`
**Partitions**: 10  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: User Service  
**Consumers**: Notification Service, Audit Service, Fraud Detection Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "user.created",
  "timestamp": "ISO-8601",
  "userId": "uuid",
  "email": "string",
  "phoneNumber": "string",
  "status": "ACTIVE|PENDING_VERIFICATION",
  "metadata": {
    "source": "REGISTRATION|ADMIN",
    "ipAddress": "string"
  }
}
```

#### Topic: `user.updated`
**Partitions**: 10  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: User Service  
**Consumers**: Audit Service, Fraud Detection Service

#### Topic: `user.verified`
**Partitions**: 10  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: User Service  
**Consumers**: Account Service, Notification Service, Audit Service

#### Topic: `user.suspended`
**Partitions**: 10  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: User Service  
**Consumers**: Account Service, Payment Service, Notification Service, Audit Service

#### Topic: `user.deleted`
**Partitions**: 10  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: User Service  
**Consumers**: Account Service, Payment Service, Audit Service

---

### 2. Account Domain

#### Topic: `account.created`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Account Service  
**Consumers**: Balance Service, Notification Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "account.created",
  "timestamp": "ISO-8601",
  "accountId": "uuid",
  "userId": "uuid",
  "accountType": "PRIMARY|SAVINGS|BUSINESS",
  "currency": "ISO-4217",
  "status": "ACTIVE|PENDING",
  "metadata": {}
}
```

#### Topic: `account.balance.updated`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Balance Service  
**Consumers**: Account Service, Reporting Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "account.balance.updated",
  "timestamp": "ISO-8601",
  "accountId": "uuid",
  "previousBalance": "decimal",
  "newBalance": "decimal",
  "changeAmount": "decimal",
  "transactionId": "uuid",
  "metadata": {}
}
```

#### Topic: `account.balance.reserved`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Balance Service  
**Consumers**: Payment Service, Transaction Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "account.balance.reserved",
  "timestamp": "ISO-8601",
  "accountId": "uuid",
  "reservationId": "uuid",
  "amount": "decimal",
  "currency": "ISO-4217",
  "paymentId": "uuid",
  "expiresAt": "ISO-8601",
  "metadata": {}
}
```

#### Topic: `account.balance.released`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Balance Service  
**Consumers**: Payment Service, Audit Service

#### Topic: `account.frozen`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Account Service  
**Consumers**: Payment Service, Balance Service, Notification Service, Audit Service

#### Topic: `account.closed`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Account Service  
**Consumers**: Payment Service, Balance Service, Notification Service, Audit Service

---

### 3. Payment Domain

#### Topic: `payment.initiated`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Payment Service  
**Consumers**: Balance Service, Fraud Detection Service, Transaction Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "payment.initiated",
  "timestamp": "ISO-8601",
  "paymentId": "uuid",
  "idempotencyKey": "string",
  "payerAccountId": "uuid",
  "payeeAccountId": "uuid",
  "amount": "decimal",
  "currency": "ISO-4217",
  "paymentType": "TRANSFER|PAYMENT|REFUND",
  "status": "INITIATED",
  "metadata": {
    "description": "string",
    "reference": "string",
    "initiatedBy": "uuid"
  }
}
```

#### Topic: `payment.processing`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Payment Service  
**Consumers**: Transaction Service, Notification Service, Audit Service

#### Topic: `payment.completed`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Payment Service  
**Consumers**: Account Service, Balance Service, Transaction Service, Notification Service, User Service, Reporting Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "payment.completed",
  "timestamp": "ISO-8601",
  "paymentId": "uuid",
  "payerAccountId": "uuid",
  "payeeAccountId": "uuid",
  "amount": "decimal",
  "currency": "ISO-4217",
  "transactionId": "uuid",
  "completedAt": "ISO-8601",
  "metadata": {}
}
```

#### Topic: `payment.failed`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Payment Service  
**Consumers**: Balance Service, Transaction Service, Notification Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "payment.failed",
  "timestamp": "ISO-8601",
  "paymentId": "uuid",
  "payerAccountId": "uuid",
  "payeeAccountId": "uuid",
  "amount": "decimal",
  "currency": "ISO-4217",
  "failureReason": "INSUFFICIENT_FUNDS|FRAUD_DETECTED|ACCOUNT_FROZEN|VALIDATION_ERROR",
  "errorCode": "string",
  "errorMessage": "string",
  "metadata": {}
}
```

#### Topic: `payment.reversed`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Payment Service  
**Consumers**: Balance Service, Transaction Service, Notification Service, Audit Service

#### Topic: `payment.cancelled`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Payment Service  
**Consumers**: Balance Service, Transaction Service, Audit Service

---

### 4. Transaction Domain

#### Topic: `transaction.created`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Transaction Service  
**Consumers**: Balance Service, Fraud Detection Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "transaction.created",
  "timestamp": "ISO-8601",
  "transactionId": "uuid",
  "paymentId": "uuid",
  "entries": [
    {
      "accountId": "uuid",
      "type": "DEBIT|CREDIT",
      "amount": "decimal",
      "currency": "ISO-4217"
    }
  ],
  "status": "PENDING",
  "metadata": {}
}
```

#### Topic: `transaction.settled`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Transaction Service  
**Consumers**: Balance Service, Account Service, Payment Service, Reporting Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "transaction.settled",
  "timestamp": "ISO-8601",
  "transactionId": "uuid",
  "paymentId": "uuid",
  "entries": [
    {
      "accountId": "uuid",
      "type": "DEBIT|CREDIT",
      "amount": "decimal",
      "currency": "ISO-4217"
    }
  ],
  "settledAt": "ISO-8601",
  "metadata": {}
}
```

#### Topic: `transaction.failed`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Transaction Service  
**Consumers**: Balance Service, Payment Service, Audit Service

#### Topic: `transaction.reversed`
**Partitions**: 30  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Transaction Service  
**Consumers**: Balance Service, Account Service, Payment Service, Audit Service

---

### 5. Fraud Detection Domain

#### Topic: `fraud.risk.assessed`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Fraud Detection Service  
**Consumers**: Payment Service, Audit Service

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "fraud.risk.assessed",
  "timestamp": "ISO-8601",
  "paymentId": "uuid",
  "riskScore": "decimal (0-100)",
  "riskLevel": "LOW|MEDIUM|HIGH|CRITICAL",
  "riskFactors": ["string"],
  "recommendation": "ALLOW|REVIEW|BLOCK",
  "metadata": {}
}
```

#### Topic: `fraud.alert.triggered`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 30 days  
**Producer**: Fraud Detection Service  
**Consumers**: Notification Service, Audit Service

#### Topic: `fraud.whitelisted`
**Partitions**: 20  
**Replication**: 3  
**Retention**: 7 days  
**Producer**: Fraud Detection Service  
**Consumers**: Payment Service, Audit Service

---

## Event Ordering Guarantees

### Per-Entity Ordering
- Events for the same entity (same partition key) are processed in order
- Achieved by partitioning on entity ID

### Cross-Entity Ordering
- Not guaranteed (different partitions)
- Use event timestamps and correlation IDs for ordering when needed

## Event Schema Registry

**Technology**: Confluent Schema Registry or Apicurio Registry

**Benefits**:
- Schema evolution support
- Backward/forward compatibility
- Schema validation
- Version management

**Schema Format**: Avro (recommended) or JSON Schema

## Dead Letter Queue (DLQ)

**Topic**: `{original-topic}.dlq`

**Purpose**: Store events that failed processing after retries

**Configuration**:
- Retention: 30 days
- Monitoring: Alert on DLQ messages
- Manual review and reprocessing

## Event Replay Strategy

### Use Cases
- Service recovery after downtime
- Bug fixes requiring reprocessing
- New service consuming historical events

### Implementation
- Kafka consumer groups with `auto.offset.reset=earliest`
- Idempotent event handlers
- Event versioning for compatibility

## Consumer Group Strategy

### Per-Service Consumer Groups
- Each service has its own consumer group
- Enables independent scaling and offset management

### Example Consumer Groups
- `user-service-consumer-group`
- `account-service-consumer-group`
- `payment-service-consumer-group`
- `transaction-service-consumer-group`
- `balance-service-consumer-group`
- `notification-service-consumer-group`
- `fraud-detection-service-consumer-group`
- `audit-service-consumer-group`
- `reporting-service-consumer-group`

## Kafka Cluster Configuration

### Broker Configuration
- **Brokers**: 3-5 nodes (production)
- **Replication Factor**: 3
- **Min In-Sync Replicas**: 2
- **Unclean Leader Election**: Disabled

### Performance Tuning
- **Batch Size**: 32KB - 1MB
- **Linger Time**: 10ms
- **Compression**: Snappy or LZ4
- **Acks**: `all` (wait for all replicas)

### Monitoring Topics
- `__consumer_offsets` - Consumer offset tracking
- `_schemas` - Schema Registry topics
- Custom metrics topics for monitoring


# MicroPay Microservices Architecture

## Service Catalog

### 1. API Gateway Service
**Service Name**: `api-gateway-service`  
**Port**: 8080  
**Technology**: Spring Cloud Gateway  
**Responsibilities**:
- Single entry point for all external requests
- Request routing to backend services
- Authentication and authorization (JWT validation)
- Rate limiting and throttling
- Request/response transformation
- CORS handling
- API versioning
- Load balancing across service instances
- Circuit breaker integration
- Request/response logging

**Dependencies**: Eureka (service discovery), Config Server, Redis (rate limiting cache)

---

### 2. User Service
**Service Name**: `user-service`  
**Port**: 8081  
**Technology**: Spring Boot 3, Spring Data JPA  
**Responsibilities**:
- User registration and profile management
- User authentication (password management)
- User verification (email, phone, KYC)
- User preferences and settings
- User activity tracking
- User search and lookup
- Account status management (active, suspended, closed)

**Database**: `micropay_user_db` (PostgreSQL)  
**Kafka Topics**:
- **Produces**: `user.created`, `user.updated`, `user.verified`, `user.suspended`, `user.deleted`
- **Consumes**: `payment.completed` (for user activity updates)

**External Integrations**: Email service, SMS service, KYC provider

---

### 3. Account Service
**Service Name**: `account-service`  
**Port**: 8082  
**Technology**: Spring Boot 3, Spring Data JPA  
**Responsibilities**:
- Wallet account creation and management
- Account balance management (read-only balance, balance snapshots)
- Account types (primary, savings, business)
- Account status (active, frozen, closed)
- Account limits and restrictions
- Multi-currency support
- Account statements generation

**Database**: `micropay_account_db` (PostgreSQL)  
**Kafka Topics**:
- **Produces**: `account.created`, `account.balance.updated`, `account.frozen`, `account.closed`
- **Consumes**: `payment.initiated`, `payment.completed`, `payment.failed`, `payment.reversed`

**Note**: Balance is eventually consistent. Real-time balance calculated from payment events.

---

### 4. Payment Service
**Service Name**: `payment-service`  
**Port**: 8083  
**Technology**: Spring Boot 3, Spring Data JPA  
**Responsibilities**:
- Payment initiation and orchestration
- Payment status management
- Payment validation (amount, currency, accounts)
- Payment routing (internal vs external)
- Payment retry logic
- Payment reconciliation
- Payment history and search
- Idempotency key management

**Database**: `micropay_payment_db` (PostgreSQL)  
**Kafka Topics**:
- **Produces**: `payment.initiated`, `payment.processing`, `payment.completed`, `payment.failed`, `payment.reversed`, `payment.cancelled`
- **Consumes**: `account.balance.reserved`, `account.balance.released`, `transaction.settled`, `transaction.failed`

**External Integrations**: Payment processors, banks (via payment processor)

---

### 5. Transaction Service
**Service Name**: `transaction-service`  
**Port**: 8084  
**Technology**: Spring Boot 3, Spring Data JPA  
**Responsibilities**:
- Transaction creation and lifecycle management
- Transaction settlement (debit/credit operations)
- Transaction reversal and refunds
- Transaction status tracking
- Double-entry bookkeeping enforcement
- Transaction reconciliation
- Transaction reporting and analytics

**Database**: `micropay_transaction_db` (PostgreSQL)  
**Kafka Topics**:
- **Produces**: `transaction.created`, `transaction.settled`, `transaction.failed`, `transaction.reversed`
- **Consumes**: `payment.processing`, `account.balance.reserved`

**Business Rules**:
- Every transaction must have debit and credit entries
- Total debits = Total credits (always)
- Transactions are immutable once settled

---

### 6. Balance Service
**Service Name**: `balance-service`  
**Port**: 8085  
**Technology**: Spring Boot 3, Spring Data JPA  
**Responsibilities**:
- Real-time balance calculation (event sourcing)
- Balance reservation (holds)
- Balance release
- Balance snapshots (for performance)
- Balance history and audit trail
- Balance validation before payments
- Balance aggregation (multi-account views)

**Database**: `micropay_balance_db` (PostgreSQL)  
**Cache**: Redis (for hot balance data)  
**Kafka Topics**:
- **Produces**: `account.balance.reserved`, `account.balance.released`, `account.balance.updated`
- **Consumes**: `transaction.settled`, `transaction.reversed`, `payment.initiated`, `payment.cancelled`

**Architecture**: Event sourcing pattern for balance calculation

---

### 7. Notification Service
**Service Name**: `notification-service`  
**Port**: 8086  
**Technology**: Spring Boot 3  
**Responsibilities**:
- Email notifications
- SMS notifications
- Push notifications (mobile apps)
- In-app notifications
- Notification templates management
- Notification preferences
- Notification delivery tracking
- Notification retry logic

**Database**: `micropay_notification_db` (PostgreSQL)  
**Kafka Topics**:
- **Consumes**: `payment.completed`, `payment.failed`, `user.created`, `account.created`, `transaction.settled`

**External Integrations**: Email provider (SendGrid/AWS SES), SMS provider (Twilio), Push notification service (FCM/APNS)

---

### 8. Fraud Detection Service
**Service Name**: `fraud-detection-service`  
**Port**: 8087  
**Technology**: Spring Boot 3  
**Responsibilities**:
- Real-time fraud scoring
- Rule-based fraud detection
- ML-based anomaly detection
- Risk assessment
- Transaction pattern analysis
- Device fingerprinting
- Geolocation validation
- Fraud alert generation

**Database**: `micropay_fraud_db` (PostgreSQL)  
**Cache**: Redis (for fraud rules and patterns)  
**Kafka Topics**:
- **Consumes**: `payment.initiated`, `user.created`, `transaction.created`
- **Produces**: `fraud.risk.assessed`, `fraud.alert.triggered`, `fraud.whitelisted`

**External Integrations**: ML model service, fraud detection APIs

---

### 9. Audit Service
**Service Name**: `audit-service`  
**Port**: 8088  
**Technology**: Spring Boot 3, Spring Data JPA  
**Responsibilities**:
- Audit log ingestion from all services
- Audit trail storage (immutable)
- Compliance reporting
- Audit log search and retrieval
- Data retention policies
- Audit log archival

**Database**: `micropay_audit_db` (PostgreSQL, time-series optimized)  
**Kafka Topics**:
- **Consumes**: All events from all services (via Kafka Connect or direct consumption)

**Note**: This service is read-heavy and write-only (no updates/deletes)

---

### 10. Reporting Service
**Service Name**: `reporting-service`  
**Port**: 8089  
**Technology**: Spring Boot 3  
**Responsibilities**:
- Financial reports generation
- Transaction reports
- User activity reports
- Revenue reports
- Custom report builder
- Scheduled report generation
- Report export (PDF, CSV, Excel)
- Report caching

**Database**: `micropay_reporting_db` (PostgreSQL, read replicas)  
**Cache**: Redis (for report caching)  
**Kafka Topics**:
- **Consumes**: `transaction.settled`, `payment.completed`, `account.balance.updated`

**Note**: Primarily read service, uses CQRS pattern

---

### 11. Config Server
**Service Name**: `config-server`  
**Port**: 8888  
**Technology**: Spring Cloud Config Server  
**Responsibilities**:
- Centralized configuration management
- Environment-specific configurations (dev, staging, prod)
- Configuration encryption (sensitive data)
- Configuration versioning
- Dynamic configuration refresh
- Configuration audit trail

**Storage**: Git repository or database

---

### 12. Service Discovery (Eureka)
**Service Name**: `eureka-server`  
**Port**: 8761  
**Technology**: Netflix Eureka  
**Responsibilities**:
- Service registration
- Service discovery
- Health check monitoring
- Service instance status
- Load balancer integration

---

## Service Communication Patterns

### Synchronous Communication
- **HTTP/REST**: Used for request-response patterns (via API Gateway)
- **gRPC**: Used for high-performance inter-service calls (optional, for internal services)
- **Protocol**: HTTPS with mTLS for inter-service communication

### Asynchronous Communication
- **Apache Kafka**: All state-changing operations and events
- **WebSocket (STOMP)**: Real-time notifications to frontend

## Service Dependencies Graph

```
API Gateway
    ├── User Service
    ├── Account Service
    ├── Payment Service
    │   ├── Account Service (balance check)
    │   ├── Transaction Service
    │   ├── Balance Service
    │   └── Fraud Detection Service
    ├── Transaction Service
    │   └── Balance Service
    ├── Balance Service
    ├── Notification Service
    └── Reporting Service

Kafka (Event Bus)
    ├── All services produce events
    └── All services consume relevant events

Eureka (Service Discovery)
    └── All services register

Config Server
    └── All services fetch configuration
```

## Deployment Characteristics

| Service | Replicas (Min) | Replicas (Max) | CPU Request | Memory Request | CPU Limit | Memory Limit |
|---------|---------------|----------------|-------------|----------------|-----------|--------------|
| API Gateway | 3 | 10 | 500m | 1Gi | 2 | 4Gi |
| User Service | 2 | 5 | 500m | 1Gi | 2 | 4Gi |
| Account Service | 2 | 5 | 500m | 1Gi | 2 | 4Gi |
| Payment Service | 3 | 10 | 1000m | 2Gi | 4 | 8Gi |
| Transaction Service | 2 | 5 | 500m | 1Gi | 2 | 4Gi |
| Balance Service | 3 | 8 | 1000m | 2Gi | 4 | 8Gi |
| Notification Service | 2 | 5 | 500m | 1Gi | 2 | 4Gi |
| Fraud Detection | 2 | 5 | 1000m | 2Gi | 4 | 8Gi |
| Audit Service | 2 | 5 | 500m | 1Gi | 2 | 4Gi |
| Reporting Service | 2 | 5 | 500m | 1Gi | 2 | 4Gi |
| Config Server | 2 | 3 | 200m | 512Mi | 1 | 2Gi |
| Eureka Server | 2 | 3 | 200m | 512Mi | 1 | 2Gi |

## Health Check Endpoints

All services expose:
- `/actuator/health` - Basic health check
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe
- `/actuator/metrics` - Prometheus metrics
- `/actuator/info` - Service information


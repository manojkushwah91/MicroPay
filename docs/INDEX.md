# MicroPay Architecture Documentation Index

## Quick Start

- **[Architecture Overview](./architecture/00-architecture-overview.md)**: Start here for a high-level understanding
- **[README.md](../README.md)**: Project overview and quick reference

## Architecture Documents

### Core Architecture

1. **[00. Architecture Overview](./architecture/00-architecture-overview.md)**
   - Executive summary
   - System architecture diagram
   - Technology stack
   - Key principles and patterns

2. **[01. Microservices Overview](./architecture/01-microservices-overview.md)**
   - Complete service catalog (12 services)
   - Service responsibilities and ports
   - Service dependencies
   - Deployment characteristics

3. **[02. Kafka Topics & Events](./architecture/02-kafka-topics-and-events.md)**
   - Topic naming conventions
   - Complete topic catalog
   - Event schemas
   - Consumer group strategy
   - Dead letter queues

4. **[03. Payment Event Flow](./architecture/03-payment-event-flow.md)**
   - End-to-end payment flow
   - Step-by-step event sequence
   - Failure scenarios and compensation
   - Idempotency guarantees

### Data & API Design

5. **[04. Database Schemas](./architecture/04-database-schemas.md)**
   - Database-per-service pattern
   - Complete schema for each service
   - Table definitions with indexes
   - Migration strategy

6. **[05. API Gateway Routing](./architecture/05-api-gateway-routing.md)**
   - Route configuration
   - Security strategy (JWT, RBAC, rate limiting)
   - Inter-service communication security
   - Monitoring and observability

### DevOps & Operations

7. **[06. CI/CD Pipeline](./architecture/06-cicd-pipeline.md)**
   - Complete pipeline stages
   - Jenkins configuration
   - Testing strategy
   - Deployment automation
   - Rollback procedures

8. **[07. Kubernetes Deployment](./architecture/07-kubernetes-deployment.md)**
   - Cluster architecture
   - Namespace strategy
   - Deployment patterns (rolling, blue-green, canary)
   - Scaling strategies (HPA, VPA)
   - Health checks and monitoring

9. **[08. Observability & Monitoring](./architecture/08-observability-monitoring.md)**
   - Metrics (Prometheus)
   - Logging (ELK Stack)
   - Distributed tracing (Zipkin/Jaeger)
   - Alerting strategy
   - Dashboards (Grafana, Kibana)

### Development

10. **[09. Monorepo Structure](./architecture/09-monorepo-structure.md)**
    - Complete folder structure
    - Service organization
    - Shared libraries
    - Infrastructure as Code
    - CI/CD configurations

## Document Navigation

### By Role

**Architects & Tech Leads**:
- Start with [Architecture Overview](./architecture/00-architecture-overview.md)
- Review [Microservices Overview](./architecture/01-microservices-overview.md)
- Understand [Kafka Topics & Events](./architecture/02-kafka-topics-and-events.md)

**Backend Developers**:
- [Microservices Overview](./architecture/01-microservices-overview.md)
- [Payment Event Flow](./architecture/03-payment-event-flow.md)
- [Database Schemas](./architecture/04-database-schemas.md)
- [Monorepo Structure](./architecture/09-monorepo-structure.md)

**DevOps Engineers**:
- [CI/CD Pipeline](./architecture/06-cicd-pipeline.md)
- [Kubernetes Deployment](./architecture/07-kubernetes-deployment.md)
- [Observability & Monitoring](./architecture/08-observability-monitoring.md)

**Security Engineers**:
- [API Gateway Routing](./architecture/05-api-gateway-routing.md) (Security section)
- [Architecture Overview](./architecture/00-architecture-overview.md) (Security Architecture)

**Product Managers**:
- [Architecture Overview](./architecture/00-architecture-overview.md)
- [Payment Event Flow](./architecture/03-payment-event-flow.md)

### By Topic

**Event-Driven Architecture**:
- [Kafka Topics & Events](./architecture/02-kafka-topics-and-events.md)
- [Payment Event Flow](./architecture/03-payment-event-flow.md)

**Microservices Design**:
- [Microservices Overview](./architecture/01-microservices-overview.md)
- [Database Schemas](./architecture/04-database-schemas.md)

**Security**:
- [API Gateway Routing](./architecture/05-api-gateway-routing.md)
- [Architecture Overview](./architecture/00-architecture-overview.md) (Security section)

**Deployment & Operations**:
- [Kubernetes Deployment](./architecture/07-kubernetes-deployment.md)
- [CI/CD Pipeline](./architecture/06-cicd-pipeline.md)
- [Observability & Monitoring](./architecture/08-observability-monitoring.md)

**Development**:
- [Monorepo Structure](./architecture/09-monorepo-structure.md)
- [Database Schemas](./architecture/04-database-schemas.md)

## Key Concepts

### Event-Driven Architecture
- All state changes flow through Kafka
- Services communicate via events
- Eventual consistency across services
- See: [Kafka Topics & Events](./architecture/02-kafka-topics-and-events.md)

### Saga Pattern
- Distributed transactions via choreography
- Compensation actions for failures
- See: [Payment Event Flow](./architecture/03-payment-event-flow.md)

### Database-per-Service
- Each service owns its database
- No shared databases
- Data sharing via APIs and events
- See: [Database Schemas](./architecture/04-database-schemas.md)

### API Gateway Pattern
- Single entry point for external access
- Authentication, authorization, rate limiting
- See: [API Gateway Routing](./architecture/05-api-gateway-routing.md)

## Quick Reference

### Service Ports
- API Gateway: 8080
- User Service: 8081
- Account Service: 8082
- Payment Service: 8083
- Transaction Service: 8084
- Balance Service: 8085
- Notification Service: 8086
- Fraud Detection: 8087
- Audit Service: 8088
- Reporting Service: 8089
- Config Server: 8888
- Eureka Server: 8761

### Key Kafka Topics
- `payment.initiated`
- `payment.completed`
- `payment.failed`
- `transaction.settled`
- `account.balance.updated`
- `fraud.risk.assessed`

### Database Names
- `micropay_user_db`
- `micropay_account_db`
- `micropay_payment_db`
- `micropay_transaction_db`
- `micropay_balance_db`
- `micropay_notification_db`
- `micropay_fraud_db`
- `micropay_audit_db`
- `micropay_reporting_db`

## Updates & Maintenance

This documentation is maintained as part of the MicroPay project. When making architectural changes:

1. Update the relevant architecture document
2. Update the [Architecture Overview](./architecture/00-architecture-overview.md) if needed
3. Update this index if new documents are added
4. Keep diagrams and examples in sync with code

## Feedback

For questions, suggestions, or corrections to this documentation, please create an issue or contact the architecture team.


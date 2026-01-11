# Observability & Monitoring Plan

## Observability Pillars

MicroPay implements the **Three Pillars of Observability**:
1. **Metrics**: Quantitative measurements over time
2. **Logs**: Discrete events with context
3. **Traces**: Request flow through distributed system

---

## 1. Metrics

### Metrics Collection

**Tool**: Prometheus  
**Exposure**: Spring Boot Actuator `/actuator/prometheus`  
**Scraping**: Prometheus scrapes metrics every 30 seconds

### Application Metrics

#### Business Metrics

**Payment Metrics**:
- `micropay_payments_initiated_total` - Counter
- `micropay_payments_completed_total` - Counter
- `micropay_payments_failed_total` - Counter (by failure reason)
- `micropay_payments_amount_total` - Counter (total payment amount)
- `micropay_payments_processing_duration_seconds` - Histogram
- `micropay_payments_by_status` - Gauge (grouped by status)

**Transaction Metrics**:
- `micropay_transactions_created_total` - Counter
- `micropay_transactions_settled_total` - Counter
- `micropay_transactions_failed_total` - Counter
- `micropay_transactions_amount_total` - Counter
- `micropay_transactions_settlement_duration_seconds` - Histogram

**Account Metrics**:
- `micropay_accounts_total` - Gauge
- `micropay_accounts_by_status` - Gauge (grouped by status)
- `micropay_account_balance_total` - Gauge (sum of all balances)
- `micropay_account_balance_by_currency` - Gauge

**User Metrics**:
- `micropay_users_total` - Gauge
- `micropay_users_active_total` - Gauge (logged in last 30 days)
- `micropay_users_registered_total` - Counter
- `micropay_users_verified_total` - Counter

#### Technical Metrics

**HTTP Metrics** (Spring Boot Actuator):
- `http_server_requests_seconds` - Histogram (request duration)
- `http_server_requests_total` - Counter (request count)
- `http_server_requests_active` - Gauge (active requests)

**JVM Metrics**:
- `jvm_memory_used_bytes` - Gauge (by memory area)
- `jvm_memory_max_bytes` - Gauge
- `jvm_gc_pause_seconds` - Histogram
- `jvm_threads_live` - Gauge
- `jvm_threads_daemon` - Gauge

**Database Metrics**:
- `hikaricp_connections_active` - Gauge
- `hikaricp_connections_idle` - Gauge
- `hikaricp_connections_pending` - Gauge
- `hikaricp_connections_timeout_total` - Counter
- `jdbc_connections_acquire_seconds` - Histogram

**Kafka Metrics**:
- `spring_kafka_consumer_records_total` - Counter
- `spring_kafka_consumer_records_lag` - Gauge (consumer lag)
- `spring_kafka_producer_record_send_total` - Counter
- `spring_kafka_producer_record_send_failed_total` - Counter
- `spring_kafka_producer_record_send_duration_seconds` - Histogram

**Redis Metrics**:
- `lettuce_commands_duration_seconds` - Histogram
- `lettuce_commands_total` - Counter
- `lettuce_commands_failed_total` - Counter

**Circuit Breaker Metrics** (Resilience4j):
- `resilience4j_circuitbreaker_calls_total` - Counter
- `resilience4j_circuitbreaker_state` - Gauge (0=closed, 1=open, 2=half-open)
- `resilience4j_circuitbreaker_failure_rate` - Gauge

### Infrastructure Metrics

**Kubernetes Metrics**:
- `kube_pod_container_resource_requests` - Gauge
- `kube_pod_container_resource_limits` - Gauge
- `kube_pod_container_resource_usage` - Gauge
- `kube_pod_status_phase` - Gauge
- `kube_deployment_status_replicas` - Gauge

**Node Metrics**:
- `node_cpu_usage_seconds_total` - Counter
- `node_memory_usage_bytes` - Gauge
- `node_disk_usage_bytes` - Gauge
- `node_network_receive_bytes_total` - Counter
- `node_network_transmit_bytes_total` - Counter

### Metrics Retention

- **Raw Metrics**: 15 days
- **5-minute Aggregates**: 30 days
- **1-hour Aggregates**: 90 days
- **1-day Aggregates**: 1 year

---

## 2. Logging

### Log Aggregation Stack

**ELK Stack**:
- **Elasticsearch**: Log storage and indexing
- **Logstash/Fluentd**: Log collection and processing
- **Kibana**: Log visualization and search

### Log Collection Architecture

```
Application Pods
    ↓ (stdout/stderr)
Fluentd DaemonSet (per node)
    ↓ (forward logs)
Logstash (centralized processing)
    ↓ (index logs)
Elasticsearch Cluster
    ↓ (query)
Kibana Dashboard
```

### Log Format

**Structured JSON Logging**:
```json
{
  "timestamp": "2024-01-15T10:00:00.123Z",
  "level": "INFO",
  "service": "payment-service",
  "traceId": "abc-123-def-456",
  "spanId": "span-789",
  "userId": "user-001",
  "paymentId": "pay-123",
  "message": "Payment initiated",
  "metadata": {
    "amount": 100.00,
    "currency": "USD"
  },
  "thread": "http-nio-8083-exec-1",
  "logger": "com.micropay.payment.PaymentService"
}
```

### Log Levels

- **ERROR**: Errors requiring immediate attention
- **WARN**: Warning conditions (e.g., retries, fallbacks)
- **INFO**: Informational messages (e.g., payment initiated, completed)
- **DEBUG**: Detailed debugging information (development only)
- **TRACE**: Very detailed tracing (development only)

### Log Retention

- **Production**: 30 days (hot), 1 year (cold storage)
- **Staging**: 14 days
- **Development**: 7 days

### Sensitive Data Masking

**Masked Fields**:
- Passwords
- Credit card numbers
- CVV
- SSN
- JWT tokens (partial masking)
- API keys

**Implementation**: Logback pattern with custom converter

---

## 3. Distributed Tracing

### Tracing Stack

**Technology**: Spring Cloud Sleuth + Zipkin / Jaeger

**Architecture**:
```
Application Services
    ↓ (trace context propagation)
Spring Cloud Sleuth
    ↓ (send spans)
Zipkin/Jaeger Collector
    ↓ (store traces)
Zipkin/Jaeger Storage (Elasticsearch)
    ↓ (query traces)
Zipkin/Jaeger UI
```

### Trace Context Propagation

**Headers**:
- `X-Trace-Id`: Unique trace identifier
- `X-Span-Id`: Current span identifier
- `X-Parent-Span-Id`: Parent span identifier
- `X-Sampled`: Sampling decision (1 or 0)

**Propagation Formats**:
- **B3**: Zipkin format (default)
- **W3C Trace Context**: Standard format

### Sampling Strategy

**Production**:
- **Head-based Sampling**: 10% of requests
- **Tail-based Sampling**: 100% of errors, slow requests (>1s)

**Staging/Development**:
- **Head-based Sampling**: 100% of requests

### Trace Spans

**Key Spans**:
1. **API Gateway**: Incoming HTTP request
2. **Service Entry**: Service receives request
3. **Database Query**: SQL execution
4. **Kafka Producer**: Event production
5. **Kafka Consumer**: Event consumption
6. **External API Call**: Third-party service calls
7. **Service Exit**: Service sends response

### Trace Retention

- **Production**: 7 days
- **Staging**: 3 days
- **Development**: 1 day

---

## 4. Alerting

### Alerting Stack

**Tool**: Prometheus Alertmanager + PagerDuty

**Flow**:
```
Prometheus (evaluates alert rules)
    ↓ (fires alerts)
Alertmanager (routes alerts)
    ↓ (sends notifications)
PagerDuty / Slack / Email
```

### Alert Rules

#### Critical Alerts (PagerDuty)

**Payment Service Down**:
```yaml
- alert: PaymentServiceDown
  expr: up{job="payment-service"} == 0
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Payment Service is down"
    description: "Payment Service has been down for more than 2 minutes"
```

**High Payment Failure Rate**:
```yaml
- alert: HighPaymentFailureRate
  expr: |
    rate(micropay_payments_failed_total[5m]) / 
    rate(micropay_payments_initiated_total[5m]) > 0.05
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "High payment failure rate"
    description: "Payment failure rate is {{ $value | humanizePercentage }}"
```

**Database Connection Pool Exhausted**:
```yaml
- alert: DatabaseConnectionPoolExhausted
  expr: hikaricp_connections_pending > 5
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Database connection pool exhausted"
```

**Kafka Consumer Lag High**:
```yaml
- alert: KafkaConsumerLagHigh
  expr: spring_kafka_consumer_records_lag > 1000
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Kafka consumer lag is high"
    description: "Consumer lag is {{ $value }} messages"
```

**High Error Rate**:
```yaml
- alert: HighErrorRate
  expr: |
    rate(http_server_requests_total{status=~"5.."}[5m]) /
    rate(http_server_requests_total[5m]) > 0.01
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "High error rate"
    description: "Error rate is {{ $value | humanizePercentage }}"
```

#### Warning Alerts (Slack)

**High CPU Usage**:
```yaml
- alert: HighCPUUsage
  expr: rate(container_cpu_usage_seconds_total[5m]) > 0.8
  for: 10m
  labels:
    severity: warning
  annotations:
    summary: "High CPU usage"
```

**High Memory Usage**:
```yaml
- alert: HighMemoryUsage
  expr: container_memory_usage_bytes / container_spec_memory_limit_bytes > 0.85
  for: 10m
  labels:
    severity: warning
  annotations:
    summary: "High memory usage"
```

**Slow Request Duration**:
```yaml
- alert: SlowRequestDuration
  expr: histogram_quantile(0.95, http_server_requests_seconds_bucket) > 2
  for: 10m
  labels:
    severity: warning
  annotations:
    summary: "Slow request duration"
    description: "P95 request duration is {{ $value }}s"
```

### Alert Routing

**Critical Alerts**:
- **PagerDuty**: Immediate notification to on-call engineer
- **Slack**: Alert to #alerts channel
- **Email**: Summary to team

**Warning Alerts**:
- **Slack**: Alert to #alerts channel
- **Email**: Daily digest

**Info Alerts**:
- **Slack**: Notification to #monitoring channel

---

## 5. Dashboards

### Grafana Dashboards

#### 1. Service Overview Dashboard

**Panels**:
- Request rate (requests/second)
- Error rate (percentage)
- Response time (p50, p95, p99)
- Active requests
- JVM memory usage
- CPU usage
- Database connection pool status

**Refresh**: 30 seconds  
**Time Range**: Last 1 hour (default)

#### 2. Payment Service Dashboard

**Panels**:
- Payment initiation rate
- Payment completion rate
- Payment failure rate (by reason)
- Payment processing duration (p50, p95, p99)
- Payment amount distribution
- Payments by status
- Kafka consumer lag
- Circuit breaker state

**Refresh**: 30 seconds

#### 3. Transaction Service Dashboard

**Panels**:
- Transaction creation rate
- Transaction settlement rate
- Transaction failure rate
- Settlement duration
- Transaction amount distribution
- Double-entry validation (debits = credits)

**Refresh**: 30 seconds

#### 4. Infrastructure Dashboard

**Panels**:
- Kubernetes pod status
- Node CPU usage
- Node memory usage
- Node disk usage
- Network I/O
- Pod restarts
- HPA scaling events

**Refresh**: 1 minute

#### 5. Kafka Dashboard

**Panels**:
- Message production rate (by topic)
- Message consumption rate (by consumer group)
- Consumer lag (by consumer group)
- Topic partition count
- Broker disk usage
- Broker CPU usage

**Refresh**: 30 seconds

#### 6. Database Dashboard

**Panels**:
- Database connection pool status (by service)
- Query execution time (p95, p99)
- Slow queries count
- Database size (by database)
- Replication lag
- Backup status

**Refresh**: 1 minute

#### 7. Business Metrics Dashboard

**Panels**:
- Total payment volume (daily, weekly, monthly)
- Total transaction count
- Active users (daily, weekly, monthly)
- New user registrations
- Account creation rate
- Revenue trends

**Refresh**: 5 minutes

### Kibana Dashboards

#### 1. Error Logs Dashboard

**Visualizations**:
- Error log count over time
- Error log distribution by service
- Error log distribution by error type
- Top error messages
- Error log trends

#### 2. Payment Flow Dashboard

**Visualizations**:
- Payment events timeline
- Payment status transitions
- Payment failure reasons
- Payment processing time distribution

#### 3. User Activity Dashboard

**Visualizations**:
- User login events
- User registration events
- User activity by hour
- Geographic distribution of users

---

## 6. Health Checks

### Application Health Endpoints

**Spring Boot Actuator**:
- `/actuator/health` - Overall health
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe
- `/actuator/health/db` - Database health
- `/actuator/health/kafka` - Kafka health
- `/actuator/health/redis` - Redis health

**Health Status**:
- **UP**: Service is healthy
- **DOWN**: Service is unhealthy
- **OUT_OF_SERVICE**: Service is temporarily unavailable

### Composite Health Checks

**Readiness Check Components**:
- Database connectivity
- Kafka connectivity
- Redis connectivity
- External service connectivity (if applicable)

**Liveness Check Components**:
- Application is responsive
- No deadlocks
- Memory is not exhausted

---

## 7. Performance Monitoring

### APM (Application Performance Monitoring)

**Tool**: New Relic / Datadog / Elastic APM

**Key Metrics**:
- **Response Time**: P50, P95, P99
- **Throughput**: Requests per second
- **Error Rate**: Percentage of failed requests
- **Apdex Score**: Application performance index
- **Database Query Performance**: Slow queries
- **External API Performance**: Third-party service latency

### Profiling

**Tool**: JProfiler / Async Profiler

**Use Cases**:
- Performance optimization
- Memory leak detection
- CPU hotspot identification

**Frequency**: On-demand or scheduled (weekly)

---

## 8. Synthetic Monitoring

### Uptime Monitoring

**Tool**: Pingdom / UptimeRobot

**Endpoints Monitored**:
- API Gateway health endpoint
- Payment API endpoint
- Account API endpoint
- User API endpoint

**Frequency**: Every 1 minute  
**Locations**: Multiple geographic locations

### Transaction Monitoring

**Tool**: Custom synthetic tests

**Scenarios**:
1. User registration → Account creation → Payment initiation → Payment completion
2. Payment failure scenario (insufficient balance)
3. Payment cancellation

**Frequency**: Every 5 minutes

---

## 9. Cost Monitoring

### Cloud Cost Tracking

**Metrics**:
- Compute costs (Kubernetes nodes)
- Storage costs (databases, backups)
- Network costs (data transfer)
- Managed service costs (Kafka, Redis)

**Dashboards**:
- Daily cost trends
- Cost by service
- Cost by environment
- Cost forecasts

**Alerts**:
- Daily cost exceeds budget
- Unusual cost spike (>20% increase)

---

## 10. Compliance & Audit

### Audit Logging

**Events Logged**:
- User authentication (login, logout)
- Payment operations (initiate, complete, cancel)
- Account operations (create, freeze, close)
- Administrative actions
- Configuration changes

**Storage**: Elasticsearch (1 year retention)

**Access**: Read-only for auditors

### Compliance Reports

**Reports Generated**:
- Payment transaction reports (for financial audits)
- User activity reports (for compliance)
- Security event reports (for security audits)

**Frequency**: Monthly, quarterly, annually

---

## 11. Incident Response

### Runbooks

**Common Incidents**:
1. **Service Down**: Check pod status, logs, metrics
2. **High Error Rate**: Check error logs, database connectivity, external services
3. **High Latency**: Check CPU/memory usage, database query performance, Kafka lag
4. **Payment Failures**: Check balance service, fraud detection, transaction service
5. **Database Issues**: Check connection pool, replication lag, disk space

### On-Call Rotation

**Schedule**: Weekly rotation  
**Escalation**: 
- Level 1: On-call engineer (15 minutes response)
- Level 2: Team lead (30 minutes response)
- Level 3: Engineering manager (1 hour response)

### Post-Incident Review

**Process**:
1. Incident timeline
2. Root cause analysis
3. Impact assessment
4. Action items
5. Follow-up tasks

**Tool**: Incident.io / PagerDuty postmortems

---

## 12. Monitoring Best Practices

1. **Monitor Business Metrics**: Not just technical metrics
2. **Set Appropriate Thresholds**: Based on historical data
3. **Avoid Alert Fatigue**: Only alert on actionable items
4. **Use SLOs/SLIs**: Define service level objectives
5. **Correlate Metrics**: Combine metrics for better insights
6. **Regular Dashboard Reviews**: Weekly team reviews
7. **Document Runbooks**: Clear incident response procedures
8. **Test Alerting**: Regular alert testing
9. **Cost Optimization**: Monitor and optimize monitoring costs
10. **Continuous Improvement**: Refine monitoring based on incidents


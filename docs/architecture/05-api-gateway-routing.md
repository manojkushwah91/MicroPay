# API Gateway Routing & Security Strategy

## API Gateway Overview

**Service**: Spring Cloud Gateway  
**Port**: 8080  
**Technology**: Spring Cloud Gateway (Reactive, WebFlux)

## Routing Strategy

### Route Configuration

All routes are configured via Spring Cloud Config Server and can be dynamically refreshed.

### Route Patterns

#### 1. User Service Routes

```
GET    /api/v1/users/me
GET    /api/v1/users/{userId}
PUT    /api/v1/users/me
PATCH  /api/v1/users/me
DELETE /api/v1/users/me
GET    /api/v1/users/me/profile
PUT    /api/v1/users/me/profile
GET    /api/v1/users/me/preferences
PUT    /api/v1/users/me/preferences
POST   /api/v1/users/register
POST   /api/v1/users/login
POST   /api/v1/users/logout
POST   /api/v1/users/refresh-token
POST   /api/v1/users/verify-email
POST   /api/v1/users/verify-phone
POST   /api/v1/users/reset-password
POST   /api/v1/users/change-password
```

**Route Configuration**:
```yaml
routes:
  - id: user-service
    uri: lb://user-service
    predicates:
      - Path=/api/v1/users/**
    filters:
      - StripPrefix=0
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenishRate: 100
          redis-rate-limiter.burstCapacity: 200
          redis-rate-limiter.requestedTokens: 1
    metadata:
      service-name: user-service
```

#### 2. Account Service Routes

```
GET    /api/v1/accounts
GET    /api/v1/accounts/{accountId}
POST   /api/v1/accounts
GET    /api/v1/accounts/{accountId}/balance
GET    /api/v1/accounts/{accountId}/statements
GET    /api/v1/accounts/{accountId}/limits
PUT    /api/v1/accounts/{accountId}/limits
```

**Route Configuration**:
```yaml
routes:
  - id: account-service
    uri: lb://account-service
    predicates:
      - Path=/api/v1/accounts/**
    filters:
      - StripPrefix=0
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenishRate: 200
          redis-rate-limiter.burstCapacity: 400
    metadata:
      service-name: account-service
```

#### 3. Payment Service Routes

```
POST   /api/v1/payments
GET    /api/v1/payments
GET    /api/v1/payments/{paymentId}
GET    /api/v1/payments/{paymentId}/status
POST   /api/v1/payments/{paymentId}/cancel
GET    /api/v1/payments/history
```

**Route Configuration**:
```yaml
routes:
  - id: payment-service
    uri: lb://payment-service
    predicates:
      - Path=/api/v1/payments/**
    filters:
      - StripPrefix=0
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenishRate: 50
          redis-rate-limiter.burstCapacity: 100
    metadata:
      service-name: payment-service
```

#### 4. Transaction Service Routes

```
GET    /api/v1/transactions
GET    /api/v1/transactions/{transactionId}
GET    /api/v1/transactions/account/{accountId}
GET    /api/v1/transactions/payment/{paymentId}
```

**Route Configuration**:
```yaml
routes:
  - id: transaction-service
    uri: lb://transaction-service
    predicates:
      - Path=/api/v1/transactions/**
    filters:
      - StripPrefix=0
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenishRate: 200
          redis-rate-limiter.burstCapacity: 400
    metadata:
      service-name: transaction-service
```

#### 5. Reporting Service Routes

```
GET    /api/v1/reports/financial
GET    /api/v1/reports/transactions
GET    /api/v1/reports/revenue
POST   /api/v1/reports/custom
GET    /api/v1/reports/{reportId}
GET    /api/v1/reports/{reportId}/download
```

**Route Configuration**:
```yaml
routes:
  - id: reporting-service
    uri: lb://reporting-service
    predicates:
      - Path=/api/v1/reports/**
    filters:
      - StripPrefix=0
      - name: RequestRateLimiter
        args:
          redis-rate-limiter.replenishRate: 10
          redis-rate-limiter.burstCapacity: 20
    metadata:
      service-name: reporting-service
```

#### 6. WebSocket Routes

```
WS     /ws/notifications
WS     /ws/payments/{paymentId}
```

**Route Configuration**:
```yaml
routes:
  - id: websocket-gateway
    uri: lb://websocket-gateway-service
    predicates:
      - Path=/ws/**
    filters:
      - StripPrefix=0
    metadata:
      service-name: websocket-gateway-service
```

---

## Security Strategy

### 1. Authentication

#### JWT Token Validation

**Filter**: Custom JWT Authentication Filter

**Process**:
1. Extract JWT token from `Authorization: Bearer <token>` header
2. Validate token signature (using public key from User Service or Config Server)
3. Validate token expiration
4. Validate token claims (issuer, audience, subject)
5. Extract user information and roles
6. Set authentication context for downstream services

**Token Structure**:
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "roles": ["USER"],
  "iat": 1234567890,
  "exp": 1234571490,
  "iss": "micropay-auth",
  "aud": "micropay-api"
}
```

#### Public Endpoints (No Authentication)

```
POST   /api/v1/users/register
POST   /api/v1/users/login
POST   /api/v1/users/reset-password
POST   /api/v1/users/verify-email
GET    /actuator/health
GET    /actuator/info
```

**Route Configuration**:
```yaml
routes:
  - id: public-endpoints
    uri: lb://user-service
    predicates:
      - Path=/api/v1/users/register,/api/v1/users/login,/api/v1/users/reset-password,/api/v1/users/verify-email
    filters:
      - StripPrefix=0
    metadata:
      authentication-required: false
```

#### Protected Endpoints (Authentication Required)

All other endpoints require valid JWT token.

**Route Configuration**:
```yaml
default-filters:
  - name: JwtAuthenticationFilter
    args:
      jwt-public-key-url: http://config-server/config/jwt/public-key
      jwt-issuer: micropay-auth
      jwt-audience: micropay-api
```

### 2. Authorization

#### Role-Based Access Control (RBAC)

**Roles**:
- `USER`: Standard user (default)
- `ADMIN`: Administrative access
- `SUPPORT`: Customer support access
- `AUDITOR`: Read-only audit access

#### Role-Based Route Filtering

**Admin-Only Routes**:
```
GET    /api/v1/admin/users
GET    /api/v1/admin/accounts
GET    /api/v1/admin/payments
GET    /api/v1/admin/transactions
GET    /api/v1/admin/reports
POST   /api/v1/admin/users/{userId}/suspend
POST   /api/v1/admin/accounts/{accountId}/freeze
```

**Route Configuration**:
```yaml
routes:
  - id: admin-routes
    uri: lb://admin-service
    predicates:
      - Path=/api/v1/admin/**
    filters:
      - StripPrefix=0
      - name: RoleAuthorizationFilter
        args:
          required-roles: ADMIN
    metadata:
      service-name: admin-service
```

#### Resource-Based Authorization

**User Resource Access**:
- Users can only access their own resources
- Validated via JWT `sub` claim vs resource owner

**Filter**: Custom ResourceAuthorizationFilter

**Example**:
```
GET /api/v1/users/{userId}/accounts
```
- Extract `userId` from path
- Extract `sub` from JWT token
- Validate: `userId == sub` OR user has `ADMIN` role

### 3. Rate Limiting

#### Strategy: Token Bucket Algorithm (Redis-based)

**Implementation**: Spring Cloud Gateway Redis Rate Limiter

#### Rate Limit Tiers

| Endpoint Type | Requests/Second | Burst Capacity |
|--------------|----------------|----------------|
| Authentication | 5 | 10 |
| Payment Creation | 10 | 20 |
| Payment Queries | 50 | 100 |
| Account Queries | 100 | 200 |
| User Queries | 100 | 200 |
| Reporting | 5 | 10 |

#### Per-User Rate Limiting

**Key**: `rate-limit:{userId}:{endpoint}`

**Configuration**:
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 100
      redis-rate-limiter.burstCapacity: 200
      redis-rate-limiter.requestedTokens: 1
      key-resolver: "#{@userKeyResolver}"
```

#### Global Rate Limiting

**Key**: `rate-limit:global:{endpoint}`

**Purpose**: Prevent DDoS attacks

**Configuration**:
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 1000
      redis-rate-limiter.burstCapacity: 2000
      key-resolver: "#{@ipKeyResolver}"
```

### 4. CORS Configuration

**Allowed Origins**:
- Production: `https://app.micropay.com`
- Staging: `https://staging.micropay.com`
- Development: `http://localhost:3000`, `http://localhost:5173`

**Configuration**:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - https://app.micropay.com
              - https://staging.micropay.com
            allowedMethods:
              - GET
              - POST
              - PUT
              - PATCH
              - DELETE
              - OPTIONS
            allowedHeaders:
              - Authorization
              - Content-Type
              - X-Requested-With
              - X-Idempotency-Key
            allowCredentials: true
            maxAge: 3600
```

### 5. Request/Response Transformation

#### Request Transformation

**Add Headers**:
- `X-Request-ID`: Unique request identifier (UUID)
- `X-User-ID`: Extracted from JWT token
- `X-Forwarded-For`: Client IP address
- `X-Request-Timestamp`: Request timestamp

**Filter Configuration**:
```yaml
filters:
  - name: AddRequestHeader
    args:
      name: X-Request-ID
      value: "#{T(java.util.UUID).randomUUID()}"
  - name: AddRequestHeader
    args:
      name: X-User-ID
      value: "#{@jwtExtractor.extractUserId()}"
```

#### Response Transformation

**Add Headers**:
- `X-Request-ID`: Echo request ID
- `X-Response-Time`: Processing time in milliseconds

**Remove Headers**:
- Internal service headers
- Sensitive information

### 6. Circuit Breaker

**Implementation**: Resilience4j Circuit Breaker

**Configuration**:
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

**Route Configuration**:
```yaml
routes:
  - id: payment-service
    uri: lb://payment-service
    filters:
      - name: CircuitBreaker
        args:
          name: paymentServiceCircuitBreaker
          fallbackUri: forward:/fallback/payment
```

**Fallback Strategy**:
- Return 503 Service Unavailable
- Log error for monitoring
- Trigger alert

### 7. Request/Response Logging

#### Logged Information

**Request**:
- Request ID
- User ID
- HTTP Method
- URI
- Headers (sanitized)
- Query parameters
- Request body (sanitized, max 1KB)

**Response**:
- Request ID
- Status code
- Response time
- Response size

#### Log Format

```
[API-GATEWAY] Request: method=POST, uri=/api/v1/payments, requestId=abc-123, userId=user-456, duration=150ms, status=200
```

#### Sensitive Data Masking

**Masked Fields**:
- `password`
- `passwordHash`
- `token`
- `authorization`
- `creditCard`
- `cvv`
- `ssn`

### 8. IP Whitelisting/Blacklisting

#### IP Whitelisting (Admin Endpoints)

**Configuration**: Redis-based IP whitelist

**Key**: `ip-whitelist:admin`

**Filter**: Custom IPWhitelistFilter

#### IP Blacklisting (DDoS Protection)

**Configuration**: Redis-based IP blacklist

**Key**: `ip-blacklist:{ip-address}`

**TTL**: 1 hour (auto-expire)

**Filter**: Custom IPBlacklistFilter

### 9. Request Size Limits

**Max Request Size**: 10 MB

**Configuration**:
```yaml
spring:
  codec:
    max-in-memory-size: 10MB
```

**Response**: 413 Payload Too Large

### 10. Timeout Configuration

**Global Timeout**: 30 seconds

**Per-Service Timeouts**:
- Payment Service: 60 seconds (long-running operations)
- Reporting Service: 120 seconds (report generation)
- Other Services: 30 seconds

**Configuration**:
```yaml
routes:
  - id: payment-service
    uri: lb://payment-service
    filters:
      - name: Hystrix
        args:
          name: paymentService
          fallbackUri: forward:/fallback/payment
      - name: RequestRateLimiter
    metadata:
      response-timeout: 60000
```

---

## Inter-Service Communication Security

### mTLS (Mutual TLS)

**Purpose**: Secure inter-service communication

**Implementation**:
- All services use mTLS certificates
- Certificates issued by internal CA
- Certificate rotation: Every 90 days

**Configuration**:
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        ssl:
          use-insecure-trust-manager: false
          trust-cert: classpath:ca-cert.pem
          key-cert: classpath:service-cert.pem
          key-cert-key: classpath:service-key.pem
```

### Service-to-Service Authentication

**Header**: `X-Service-Auth`

**Value**: Service-specific API key (stored in Config Server)

**Validation**: Custom ServiceAuthFilter

---

## Monitoring & Observability

### Metrics

**Key Metrics**:
- Request rate (requests/second)
- Response time (p50, p95, p99)
- Error rate (4xx, 5xx)
- Circuit breaker state
- Rate limiter rejections
- Active connections

**Exposed Endpoint**: `/actuator/metrics`

### Distributed Tracing

**Implementation**: Spring Cloud Sleuth + Zipkin

**Trace ID**: Propagated via `X-Trace-ID` header

**Span Tags**:
- `service.name`: API Gateway
- `http.method`: GET, POST, etc.
- `http.status_code`: 200, 404, etc.
- `user.id`: User ID from JWT
- `request.id`: Request ID

---

## Configuration Management

### Dynamic Route Refresh

**Endpoint**: `POST /actuator/gateway/refresh`

**Trigger**: Config Server change webhook

**Process**:
1. Config Server detects change
2. Sends webhook to API Gateway
3. API Gateway refreshes routes
4. New routes take effect (zero downtime)

### Environment-Specific Configuration

**Files**:
- `application.yml`: Base configuration
- `application-dev.yml`: Development overrides
- `application-staging.yml`: Staging overrides
- `application-prod.yml`: Production overrides

**Source**: Spring Cloud Config Server


# MicroPay Monorepo Structure

## Overview

This document defines the monorepo folder structure for the MicroPay platform. The structure is designed to support:
- Independent service development
- Shared libraries and utilities
- Centralized configuration
- CI/CD pipeline integration
- Easy navigation and discovery

---

## Root Structure

```
MicroPay/
├── .github/                          # GitHub workflows and templates
│   ├── workflows/
│   │   ├── ci.yml                   # CI workflow
│   │   └── cd.yml                   # CD workflow
│   └── ISSUE_TEMPLATE/
│
├── .cursor/                          # Cursor IDE configuration
│   └── rules/                        # Cursor rules for AI assistance
│
├── docs/                             # Documentation
│   ├── architecture/                 # Architecture design documents
│   │   ├── 01-microservices-overview.md
│   │   ├── 02-kafka-topics-and-events.md
│   │   ├── 03-payment-event-flow.md
│   │   ├── 04-database-schemas.md
│   │   ├── 05-api-gateway-routing.md
│   │   ├── 06-cicd-pipeline.md
│   │   ├── 07-kubernetes-deployment.md
│   │   └── 08-observability-monitoring.md
│   ├── api/                          # API documentation
│   │   ├── openapi/                  # OpenAPI/Swagger specs
│   │   └── postman/                  # Postman collections
│   ├── runbooks/                     # Operational runbooks
│   └── guides/                       # Developer guides
│
├── services/                         # Microservices
│   ├── api-gateway/                  # API Gateway Service
│   ├── user-service/                 # User Service
│   ├── account-service/              # Account Service
│   ├── payment-service/              # Payment Service
│   ├── transaction-service/          # Transaction Service
│   ├── balance-service/              # Balance Service
│   ├── notification-service/         # Notification Service
│   ├── fraud-detection-service/      # Fraud Detection Service
│   ├── audit-service/                # Audit Service
│   ├── reporting-service/            # Reporting Service
│   ├── config-server/                # Config Server
│   └── eureka-server/                # Eureka Server
│
├── libs/                             # Shared libraries
│   ├── common/                       # Common utilities
│   ├── kafka/                        # Kafka utilities
│   ├── security/                     # Security utilities
│   ├── monitoring/                   # Monitoring utilities
│   └── testing/                      # Testing utilities
│
├── infrastructure/                    # Infrastructure as Code
│   ├── kubernetes/                   # Kubernetes manifests
│   │   ├── base/                     # Base manifests
│   │   ├── overlays/                 # Environment overlays
│   │   │   ├── development/
│   │   │   ├── staging/
│   │   │   └── production/
│   │   └── helm/                     # Helm charts
│   ├── docker/                       # Docker configurations
│   │   ├── docker-compose.dev.yml   # Development compose
│   │   ├── docker-compose.test.yml  # Test compose
│   │   └── base-images/              # Base Docker images
│   ├── terraform/                    # Terraform (if used)
│   └── scripts/                      # Infrastructure scripts
│
├── ci-cd/                            # CI/CD configurations
│   ├── jenkins/                      # Jenkins pipelines
│   │   ├── Jenkinsfile.template      # Pipeline template
│   │   └── shared-libraries/         # Jenkins shared libraries
│   └── scripts/                      # CI/CD scripts
│
├── database/                          # Database scripts
│   ├── migrations/                   # Database migrations
│   │   ├── user-service/
│   │   ├── account-service/
│   │   ├── payment-service/
│   │   └── ...
│   └── seeds/                        # Seed data
│
├── kafka/                             # Kafka configurations
│   ├── topics/                       # Topic definitions
│   ├── schemas/                      # Avro/JSON schemas
│   └── connectors/                   # Kafka Connect configs
│
├── frontend/                          # Frontend application
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
│
├── scripts/                           # Utility scripts
│   ├── setup/                        # Setup scripts
│   ├── deployment/                  # Deployment scripts
│   └── monitoring/                  # Monitoring scripts
│
├── tests/                             # Integration and E2E tests
│   ├── integration/                  # Integration tests
│   ├── e2e/                          # End-to-end tests
│   └── performance/                  # Performance tests
│
├── .gitignore
├── .editorconfig
├── LICENSE
├── README.md                          # Main README
└── pom.xml                            # Parent POM (Maven) or build.gradle (Gradle)
```

---

## Service Structure (Example: payment-service)

Each service follows a consistent structure:

```
services/payment-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/micropay/payment/
│   │   │       ├── PaymentServiceApplication.java
│   │   │       ├── config/           # Configuration classes
│   │   │       │   ├── KafkaConfig.java
│   │   │       │   ├── DatabaseConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── controller/      # REST controllers
│   │   │       │   └── PaymentController.java
│   │   │       ├── service/         # Business logic
│   │   │       │   ├── PaymentService.java
│   │   │       │   └── PaymentValidationService.java
│   │   │       ├── repository/      # Data access
│   │   │       │   └── PaymentRepository.java
│   │   │       ├── model/           # Domain models
│   │   │       │   ├── Payment.java
│   │   │       │   └── PaymentStatus.java
│   │   │       ├── dto/             # Data transfer objects
│   │   │       │   ├── PaymentRequest.java
│   │   │       │   └── PaymentResponse.java
│   │   │       ├── event/           # Event handlers
│   │   │       │   ├── producer/    # Kafka producers
│   │   │       │   │   └── PaymentEventProducer.java
│   │   │       │   └── consumer/    # Kafka consumers
│   │   │       │       └── BalanceEventConsumer.java
│   │   │       ├── exception/       # Exception handlers
│   │   │       │   └── PaymentException.java
│   │   │       └── util/            # Utilities
│   │   │           └── IdempotencyUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-staging.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/        # Flyway migrations
│   │           └── V1.0.0__create_payments_table.sql
│   └── test/
│       ├── java/
│       │   └── com/micropay/payment/
│       │       ├── PaymentServiceTest.java
│       │       ├── PaymentControllerTest.java
│       │       └── integration/     # Integration tests
│       │           └── PaymentIntegrationTest.java
│       └── resources/
│           └── application-test.yml
├── Dockerfile
├── Jenkinsfile
├── pom.xml                           # Maven POM
└── README.md                         # Service-specific README
```

---

## Shared Libraries Structure

### Common Library

```
libs/common/
├── src/
│   ├── main/java/com/micropay/common/
│   │   ├── exception/               # Common exceptions
│   │   ├── util/                    # Common utilities
│   │   ├── dto/                     # Common DTOs
│   │   └── constant/                # Constants
│   └── test/
└── pom.xml
```

### Kafka Library

```
libs/kafka/
├── src/
│   ├── main/java/com/micropay/kafka/
│   │   ├── producer/                # Kafka producer utilities
│   │   ├── consumer/                # Kafka consumer utilities
│   │   ├── serializer/              # Custom serializers
│   │   └── config/                  # Kafka configuration
│   └── test/
└── pom.xml
```

### Security Library

```
libs/security/
├── src/
│   ├── main/java/com/micropay/security/
│   │   ├── jwt/                     # JWT utilities
│   │   ├── encryption/              # Encryption utilities
│   │   └── auth/                    # Authentication utilities
│   └── test/
└── pom.xml
```

---

## Infrastructure Structure

### Kubernetes Manifests

```
infrastructure/kubernetes/
├── base/                             # Base Kubernetes manifests
│   ├── namespaces/
│   │   └── namespaces.yaml
│   ├── services/
│   │   ├── api-gateway/
│   │   ├── user-service/
│   │   └── ...
│   └── configmaps/
│       └── base-config.yaml
├── overlays/                         # Environment-specific overlays
│   ├── development/
│   │   ├── kustomization.yaml
│   │   └── patches/
│   ├── staging/
│   │   ├── kustomization.yaml
│   │   └── patches/
│   └── production/
│       ├── kustomization.yaml
│       └── patches/
└── helm/                             # Helm charts
    └── micropay-services/
        ├── Chart.yaml
        ├── values.yaml
        └── templates/
```

### Docker Compose

```
infrastructure/docker/
├── docker-compose.dev.yml            # Development environment
├── docker-compose.test.yml           # Test environment
└── base-images/
    ├── java-base/
    │   └── Dockerfile
    └── node-base/
        └── Dockerfile
```

---

## CI/CD Structure

```
ci-cd/
├── jenkins/
│   ├── Jenkinsfile.template          # Template for service pipelines
│   └── shared-libraries/
│       └── vars/
│           ├── micropayBuild.groovy
│           ├── micropayTest.groovy
│           ├── micropayDeploy.groovy
│           └── micropayKubernetes.groovy
└── scripts/
    ├── build.sh
    ├── test.sh
    ├── deploy.sh
    └── rollback.sh
```

---

## Database Structure

```
database/
├── migrations/
│   ├── user-service/
│   │   ├── V1.0.0__create_users_table.sql
│   │   ├── V1.0.1__create_user_profiles_table.sql
│   │   └── V1.1.0__add_user_preferences.sql
│   ├── account-service/
│   │   └── ...
│   └── payment-service/
│       └── ...
└── seeds/
    ├── development/
    │   └── seed_data.sql
    └── test/
        └── test_data.sql
```

---

## Kafka Structure

```
kafka/
├── topics/
│   ├── payment-topics.yaml          # Topic definitions
│   ├── account-topics.yaml
│   └── user-topics.yaml
├── schemas/
│   ├── avro/                        # Avro schemas
│   │   ├── PaymentInitiated.avsc
│   │   └── PaymentCompleted.avsc
│   └── json/                        # JSON schemas
│       └── payment-events.json
└── connectors/
    ├── postgres-connector.json
    └── elasticsearch-connector.json
```

---

## Frontend Structure

```
frontend/
├── src/
│   ├── components/                  # React components
│   │   ├── common/                 # Common components
│   │   ├── payment/                # Payment components
│   │   └── account/                # Account components
│   ├── pages/                      # Page components
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   └── PaymentPage.tsx
│   ├── services/                   # API services
│   │   ├── api.ts
│   │   ├── paymentService.ts
│   │   └── accountService.ts
│   ├── hooks/                     # Custom React hooks
│   ├── store/                     # State management (Redux/Zustand)
│   ├── utils/                     # Utilities
│   ├── types/                     # TypeScript types
│   └── App.tsx
├── public/
├── package.json
├── vite.config.ts
├── tsconfig.json
└── README.md
```

---

## Testing Structure

```
tests/
├── integration/
│   ├── payment-flow/
│   │   └── PaymentFlowTest.java
│   └── user-registration/
│       └── UserRegistrationTest.java
├── e2e/
│   ├── cypress/                    # Cypress E2E tests
│   │   ├── e2e/
│   │   │   ├── payment.cy.ts
│   │   │   └── account.cy.ts
│   │   └── support/
│   └── playwright/                 # Playwright E2E tests
│       └── ...
└── performance/
    ├── k6/                         # k6 performance tests
    │   └── payment-load-test.js
    └── jmeter/                     # JMeter tests
        └── payment-stress-test.jmx
```

---

## Scripts Structure

```
scripts/
├── setup/
│   ├── setup-dev-environment.sh
│   ├── setup-kafka.sh
│   └── setup-databases.sh
├── deployment/
│   ├── deploy-service.sh
│   ├── rollback-service.sh
│   └── update-config.sh
└── monitoring/
    ├── check-health.sh
    ├── check-metrics.sh
    └── generate-report.sh
```

---

## Build System

### Maven (Java Services)

**Parent POM** (`pom.xml`):
```xml
<project>
    <groupId>com.micropay</groupId>
    <artifactId>micropay-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>services/api-gateway</module>
        <module>services/user-service</module>
        <!-- ... other services ... -->
        <module>libs/common</module>
        <module>libs/kafka</module>
        <!-- ... other libraries ... -->
    </modules>
</project>
```

### NPM/PNPM (Frontend)

**Root `package.json`**:
```json
{
  "name": "micropay-monorepo",
  "private": true,
  "workspaces": [
    "frontend"
  ]
}
```

---

## Version Control Strategy

### Branching Strategy

- **main**: Production-ready code
- **develop**: Integration branch
- **feature/***: Feature branches
- **hotfix/***: Production hotfixes
- **release/***: Release candidates

### Commit Convention

**Format**: `type(scope): subject`

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build/tooling changes

**Examples**:
- `feat(payment): add payment cancellation`
- `fix(account): fix balance calculation`
- `docs(api): update API documentation`

---

## IDE Configuration

### Cursor Rules

`.cursor/rules/micropay.md`:
```markdown
# MicroPay Development Rules

- Follow Java coding standards
- Use Spring Boot best practices
- Write unit tests for all business logic
- Document public APIs
- Follow microservices patterns
```

### EditorConfig

`.editorconfig`:
```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
trim_trailing_whitespace = true
insert_final_newline = true

[*.{yml,yaml}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

---

## Documentation Standards

### Service README Template

Each service should have a README.md with:
- Service overview
- API endpoints
- Configuration
- Local development setup
- Testing instructions
- Deployment notes

### API Documentation

- OpenAPI/Swagger specs in `docs/api/openapi/`
- Postman collections in `docs/api/postman/`
- Keep documentation in sync with code

---

## Best Practices

1. **Consistent Structure**: All services follow the same structure
2. **Shared Libraries**: Common code in `libs/`
3. **Configuration Management**: Environment-specific configs in `resources/`
4. **Database Migrations**: Versioned migrations in each service
5. **Testing**: Unit, integration, and E2E tests
6. **Documentation**: Keep docs up-to-date
7. **Code Quality**: SonarQube integration
8. **Security**: Regular dependency updates
9. **Monitoring**: Metrics and logging in all services
10. **CI/CD**: Automated testing and deployment


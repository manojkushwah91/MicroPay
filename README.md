<<<<<<< HEAD
# 💳 MicroPAY – Digital Wallet & Payment Backend System

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)
![Kafka](https://img.shields.io/badge/Event_Driven-Kafka-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)

**MicroPAY** is a backend-first, event-driven digital wallet and payment processing platform designed using modern microservices principles. It focuses on **consistency, scalability, and fault tolerance**, mirroring architectural patterns commonly found in real-world fintech systems.

This project intentionally prioritizes **backend complexity over UI**, addressing challenges such as distributed state management, asynchronous workflows, and secure financial transactions.
=======
# MicroPay — Cloud-native AWS Free Tier deployment

MicroPay is an event-driven microservices system (Spring Boot + Kafka + PostgreSQL) with a React frontend. This repo is upgraded to be **fully deployable on AWS Free Tier** while keeping the backend within a **single `t2.micro` (1GB RAM)** constraint.

## Architecture
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

```mermaid
flowchart LR
  U[User Browser] -->|HTTP 80| N[Nginx reverse proxy]
  N -->|/| FE[Frontend (Nginx static)]
  N -->|/api| GW[API Gateway (Spring Cloud Gateway)]

<<<<<<< HEAD
## 🚀 Key Objectives

* Model real-world fintech backend workflows
* Demonstrate event-driven microservices using Kafka
* Ensure transactional integrity across services
* Apply security best practices for authentication and authorization
* Enable horizontal scalability and independent service evolution
=======
  subgraph Backend (Docker Compose on 1x EC2)
    GW --> AUTH[auth-service]
    GW --> WAL[wallet-service]
    GW --> PAY[payment-service]
    GW --> TX[transaction-service]
    GW --> NOTIF[notification-service]
    AUTH --> PG[(PostgreSQL)]
    WAL --> PG
    PAY --> PG
    TX --> PG
    NOTIF --> PG
    AUTH <--> K[(Kafka)]
    WAL <--> K
    PAY <--> K
    TX <--> K
    NOTIF <--> K
    K --> ZK[(ZooKeeper)]
  end

  subgraph AWS Static Hosting (Optional)
    CF[CloudFront] --> S3[(S3 Static Website)]
  end
```

### Observability & Tooling Overview
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

```mermaid
flowchart LR
  subgraph Monitoring
    P[Prometheus] --> G[Grafana Dashboards]
  end

<<<<<<< HEAD
## 🧩 High-Level Architecture

MicroPAY is composed of **loosely coupled domain services**, each owning its data and business logic.

### Communication Patterns

* **External traffic:** REST via API Gateway
* **Internal service communication:** Asynchronous events via Kafka
* **Service discovery & configuration:** Spring Cloud ecosystem

This hybrid approach ensures:

* Reduced coupling between services
* Improved resiliency under partial failures
* Eventual consistency where strict ACID guarantees are impractical

---

## 🧠 Core Domain Services

### 👤 User Management Service

* User onboarding and identity management
* JWT-based authentication and authorization
* Emits domain events for downstream services

### 💼 Wallet Management Service

* Automatic wallet provisioning per user
* Maintains wallet balances
* Subscribes to transaction and payment events

### 💸 Transaction Service

* Immutable transaction ledger
* Tracks transaction lifecycle states
* Guarantees auditability and traceability

### 🔁 Payment Service

* Validates wallet-to-wallet transfers
* Enforces balance checks and business rules
* Coordinates transaction execution via events

### 🏗 Infrastructure Services

* API Gateway (routing, auth filtering)
* Eureka Service Registry
* Centralized Configuration Server

---

## 🛠 Technology Stack

### Backend & Frameworks

* Java 17
* Spring Boot
* Spring Data JPA
* Spring Security

### Microservices Infrastructure

* Spring Cloud (Eureka, API Gateway, Config Server)

### Messaging & Events

* Apache Kafka
* Event-based state propagation
* Idempotent consumers and retry handling

### Data Layer

* PostgreSQL
* Database-per-service pattern
* Strong ownership boundaries

### Security

* JWT authentication
* Stateless authorization
* Gateway-level request validation

### DevOps

* Docker
* Docker Compose for local orchestration
* Maven-based builds
* Git-based version control
=======
  subgraph Tracing
    OTEL[OpenTelemetry SDK] --> ZK[Zipkin/Jaeger (pluggable backend)]
  end

  GW[API Gateway] -->|/actuator/prometheus| P
  AUTH[auth-service] -->|metrics| P
  WAL[wallet-service] -->|metrics| P
  PAY[payment-service] -->|metrics| P
  TX[transaction-service] -->|metrics| P
  NOTIF[notification-service] -->|metrics| P

  AUTH -.trace.-> OTEL
  WAL  -.trace.-> OTEL
  PAY  -.trace.-> OTEL
  TX   -.trace.-> OTEL
  NOTIF -.trace.-> OTEL
```

### API Documentation

- **OpenAPI / Swagger UI** is enabled for all core domain services:
  - Auth: `http://localhost:8081/swagger-ui.html`
  - Wallet: `http://localhost:8083/swagger-ui.html`
  - Payment: `http://localhost:8084/swagger-ui.html`
  - Transaction: `http://localhost:8085/swagger-ui.html`
  - Notification: `http://localhost:8086/swagger-ui.html`

Swagger UIs are available locally when running the services via Docker Compose or directly via Maven.
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

### Metrics & Dashboards

<<<<<<< HEAD
## 🤖 AI-Assisted Development Workflow

This project was built using an **AI-augmented engineering approach** to increase development velocity while maintaining high code quality.

### How AI Was Used

* Microservice scaffolding and boilerplate generation
* JPA entity modeling and repository creation
* Unit and integration test generation
* Refactoring Kafka consumer logic
* Faster debugging of cross-service failures and configuration issues

AI acted as a **pair programmer**, with all architectural and design decisions validated manually.
=======
- All Spring Boot services expose **Prometheus metrics** at `/actuator/prometheus`.
- `infrastructure/docker/prometheus.yml` scrapes:
  - `auth-service`, `wallet-service`, `payment-service`, `transaction-service`, `notification-service`
  - (Gateway can be extended in the same way if desired.)
- `docker-compose.prod.yml` includes:
  - `prometheus` (port `9090`)
  - `grafana` (port `3001`, default `admin/admin`)

To explore dashboards:

```bash
cd infrastructure/docker
docker compose -f docker-compose.prod.yml --env-file ../../.env up -d --build
# then open:
# - http://localhost:9090  (Prometheus)
# - http://localhost:3001  (Grafana)
```

### API Smoke Tests & Load Tests

- **HTTP smoke tests** (curl-style) live in `tests/smoke/api-smoke.test.http`.
  - Can be executed with REST Client plugins or imported into Postman.
- **k6 load script** in `tests/load/k6-smoke.js`:

```bash
k6 run tests/load/k6-smoke.js \
  -e BASE_URL=http://localhost \
  -e K6_EMAIL=portfolio-user@example.com \
  -e K6_PASSWORD=Password123!
```
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)


<<<<<<< HEAD
## 📂 Repository Structure

```text
📦 micropay-backend
 ┣ 📂 docs
 ┃ ┗ Architecture diagrams, API specs, and flow documentation
 ┣ 📂 frontend
 ┃ ┗ Minimal UI for interacting with backend APIs
 ┣ 📂 infrastructure
 ┃ ┗ Config Server, Eureka, API Gateway
 ┣ 📂 services
 ┃ ┣ user-service
 ┃ ┣ wallet-service
 ┃ ┣ transaction-service
 ┃ ┗ payment-service
 ┣ 📜 docker-compose.yml
 ┣ 📜 README.md
 ┗ 📜 DEPLOYMENT_FINAL.md
```
=======
## What’s included (production-ready files)

- **Optimized Dockerfiles** for Spring services (Java 17, multi-stage Maven build, slim runtime, `JAVA_OPTS="-Xms128m -Xmx256m"`).
- **`docker-compose.prod.yml`**: single network, healthchecks, restart policies, lightweight Kafka tuning, Postgres volume, dependency order, and **Nginx reverse proxy**.
- **Terraform (AWS)**:
  - EC2 `t2.micro` (Amazon Linux 2023) + Security Group + 20GB EBS
  - ECR repositories for all images
  - S3 static website + CloudFront distribution for frontend
- **GitHub Actions CI/CD**:
  - Build services and Docker images
  - Push images to ECR
  - SSH deploy to EC2 and restart Docker Compose
  - Optional frontend deploy to S3 + CloudFront invalidation
- **`.env.example`**: production environment template.
- **Spring `prod` profiles** for all runtime services: disables config/eureka, uses direct service URLs, reduces DB pool sizes, and uses structured console logging.
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

## Local: run production compose

<<<<<<< HEAD
## 🧪 Reliability & Consistency Guarantees

* Event-driven workflows with eventual consistency
* Idempotent Kafka consumers
* Explicit transaction boundaries per service
* Clear separation of command execution and state propagation
=======
1. Copy environment file:

```bash
cp .env.example .env
```
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

2. Start production stack:

<<<<<<< HEAD
## 📦 Running Locally

```bash
docker-compose up --build
```

All services, Kafka brokers, and databases will be started locally using Docker Compose.
=======
```bash
cd infrastructure/docker
docker compose -f docker-compose.prod.yml --env-file ../../.env up -d --build
```

3. Open:
- `http://localhost/` (frontend via Nginx)
- `http://localhost/api/actuator/health` (gateway health)

## AWS: provisioning with Terraform
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

### Prerequisites
- Terraform \(>= 1.5\)
- AWS account (Free Tier)
- An EC2 Key Pair name (for SSH)

<<<<<<< HEAD
## 📘 Documentation

* `docs/` – Architecture diagrams and service contracts
* `DEPLOYMENT_FINAL.md` – Production deployment notes
* OpenAPI specifications available per service
=======
### Deploy infrastructure

```bash
cd infrastructure/terraform
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform apply
```
>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

Terraform outputs:
- **EC2 public IP/DNS**
- **ECR repo URLs**
- **S3 bucket + CloudFront domain** (frontend)

<<<<<<< HEAD
## 🧠 Final Notes

MicroPAY is designed to surface the **hard parts of backend engineering**—distributed transactions, event orchestration, and service autonomy—while remaining readable, testable, and extensible.
=======
## AWS: deploy backend (EC2 Docker Compose)

On the EC2 instance, the project is deployed into `/opt/micropay` using `infrastructure/docker/docker-compose.prod.yml`.

- **Ports**: 80 (Nginx), 8080 (Gateway direct), 22 (SSH). 443 is opened for future TLS.

## CI/CD: GitHub Actions

Workflow: `.github/workflows/deploy.yml` (runs on push to `main`)

### Required GitHub Secrets

- `AWS_REGION`
- `AWS_ACCOUNT_ID`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `EC2_HOST` (public IP/DNS)
- `EC2_USER` (usually `ec2-user`)
- `EC2_SSH_PRIVATE_KEY` (private key for the EC2 keypair)
- `JWT_SECRET`
- `POSTGRES_PASSWORD`

Optional (frontend S3/CloudFront deploy job):
- `FRONTEND_S3_BUCKET` (Terraform output `frontend_s3_bucket`)
- `CLOUDFRONT_DISTRIBUTION_ID` (CloudFront distribution id)
- `FRONTEND_API_BASE_URL` (e.g. `http://<EC2_PUBLIC_IP>/api`)

## Memory & Free Tier notes (1GB RAM)

- **JVM**: containers default to `JAVA_OPTS="-Xms128m -Xmx256m"`.
- **Kafka/ZooKeeper**: tuned heap sizes and small retention.
- **DB pools**: prod profiles reduce Hikari pools \(max 4\).

If the EC2 instance OOMs, reduce concurrency (fewer requests), and consider lowering heap for the least critical services (keeping within your constraints).

## Repo paths you’ll use most

- `infrastructure/docker/docker-compose.prod.yml`
- `infrastructure/docker/nginx/nginx.conf`
- `infrastructure/terraform/`
- `.github/workflows/deploy.yml`
- `.env.example`

>>>>>>> 88e6a02 (feat: update frontend API integrations, workflows, and system documentation)

# 💳 MicroPAY – Digital Wallet & Payment Backend System

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)
![Kafka](https://img.shields.io/badge/Event_Driven-Kafka-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)

**MicroPAY** is a backend-first, event-driven digital wallet and payment processing platform designed using modern microservices principles. It focuses on **consistency, scalability, and fault tolerance**, mirroring architectural patterns commonly found in real-world fintech systems.

This project intentionally prioritizes **backend complexity over UI**, addressing challenges such as distributed state management, asynchronous workflows, and secure financial transactions.

---

## 🚀 Key Objectives

* Model real-world fintech backend workflows
* Demonstrate event-driven microservices using Kafka
* Ensure transactional integrity across services
* Apply security best practices for authentication and authorization
* Enable horizontal scalability and independent service evolution

---

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

---

## 🤖 AI-Assisted Development Workflow

This project was built using an **AI-augmented engineering approach** to increase development velocity while maintaining high code quality.

### How AI Was Used

* Microservice scaffolding and boilerplate generation
* JPA entity modeling and repository creation
* Unit and integration test generation
* Refactoring Kafka consumer logic
* Faster debugging of cross-service failures and configuration issues

AI acted as a **pair programmer**, with all architectural and design decisions validated manually.

---

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

---

## 🧪 Reliability & Consistency Guarantees

* Event-driven workflows with eventual consistency
* Idempotent Kafka consumers
* Explicit transaction boundaries per service
* Clear separation of command execution and state propagation

---

## 📦 Running Locally

```bash
docker-compose up --build
```

All services, Kafka brokers, and databases will be started locally using Docker Compose.

---

## 📘 Documentation

* `docs/` – Architecture diagrams and service contracts
* `DEPLOYMENT_FINAL.md` – Production deployment notes
* OpenAPI specifications available per service

---

## 🧠 Final Notes

MicroPAY is designed to surface the **hard parts of backend engineering**—distributed transactions, event orchestration, and service autonomy—while remaining readable, testable, and extensible.

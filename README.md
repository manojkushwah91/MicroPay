# 💳 MicroPay – Digital Wallet & Payment Backend System

MicroPay is a **backend-focused digital wallet and payment system** built using **Java and Spring Boot**, following a **microservices and event-driven architecture**.  
The system is designed to handle **users, wallets, transactions, and payments** with a strong emphasis on **data consistency, security, and scalability**.

This project was built to simulate **real-world backend challenges** commonly found in fintech systems.

---

## 🧩 System Overview

MicroPay consists of multiple backend services, each responsible for a specific domain:
- User Management
- Wallet Management
- Transaction Processing
- Payment Handling

Services communicate using **REST APIs** and **Apache Kafka** for asynchronous, event-driven workflows.

---

## 🛠️ Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- Spring Cloud (Eureka, API Gateway)

### Messaging
- Apache Kafka (event-driven communication)

### Database
- PostgreSQL (separate schema per service)

### Security
- JWT-based authentication and authorization

### Tools
- Docker
- Maven
- Git

---

## ⚙️ Core Features

### 👤 User & Wallet Management
- User registration and authentication
- Automatic wallet creation on user onboarding
- Secure access using JWT tokens

### 💰 Transaction & Payment Processing
- Wallet-to-wallet fund transfers
- Balance updates handled asynchronously
- Transaction history tracking

### 📢 Event-Driven Architecture
- Used **Kafka** to publish transaction and wallet events
- Enabled loose coupling between services
- Improved system responsiveness and scalability

### 🗄️ Data Consistency
- Database transactions handled using Spring’s transactional support
- Designed workflows to maintain consistency during partial failures
- Ensured reliable balance updates in transaction flows

### 🐳 Containerization
- Backend services containerized using **Docker**
- Enabled consistent local development and testing environments

---

## 🔄 High-Level Transaction Flow

1. User initiates a payment request via REST API  
2. Payment service validates the request and publishes a transaction event  
3. Wallet service consumes the event and updates wallet balances  
4. Transaction status is updated and persisted in the database  
5. Final response is returned to the client  

---

## 📌 Design Focus

- Backend-first microservices architecture
- Clear separation of responsibilities per service
- REST APIs for synchronous communication
- Kafka for asynchronous transaction processing
- Secure APIs using JWT
- Production-like local setup using Docker

---

## 🚀 Running the Project Locally

> Prerequisites:
- Java 17+
- Docker
- PostgreSQL
- Kafka (local or Docker-based)

Basic steps:
1. Clone the repository
2. Configure database and Kafka properties
3. Build the services using Maven
4. Run services locally or using Docker

---

## 🎯 Learning Outcomes

- Built a **microservices-based backend system**
- Implemented **event-driven communication** using Kafka
- Designed secure REST APIs with **Spring Security & JWT**
- Worked with **PostgreSQL and transactional data**
- Gained hands-on experience with **Dockerized backend services**

---

## 📬 Contact

**Manoj Kushwah**  
📧 manojkushwah91115@gmail.com  
🔗 GitHub: https://github.com/manojkushwah91  
🔗 LinkedIn: https://linkedin.com/in/manojkushwah871

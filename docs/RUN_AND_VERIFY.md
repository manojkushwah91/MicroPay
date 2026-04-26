# Run and Verify MicroPay

## Prerequisites

- Docker Desktop (with Docker Compose v2 or standalone docker-compose)

## Local run (full stack with Config Server)

1. Create env file:
   ```bash
   cd infrastructure/docker
   copy .env.example .env
   # Edit .env: set POSTGRES_PASSWORD (required)
   ```

2. Start stack:
   ```powershell
   .\scripts\run-local.ps1
   ```
   Or manually:
   ```bash
   cd infrastructure/docker
   docker compose --env-file .env up -d --build
   ```

3. After ~2–3 minutes, verify:
   ```powershell
   .\scripts\verify-stack.ps1
   ```

4. Open:
   - Frontend (via Nginx reverse proxy): http://localhost
   - API Gateway (direct): http://localhost:8080
   - Eureka: http://localhost:8761
   - Swagger UI:
     - API Gateway: http://localhost:8080/swagger-ui.html
     - Auth: http://localhost:8081/swagger-ui.html
     - User: http://localhost:8082/swagger-ui.html
     - Wallet: http://localhost:8083/swagger-ui.html
     - Payment: http://localhost:8084/swagger-ui.html
     - Transaction: http://localhost:8085/swagger-ui.html
     - Notification: http://localhost:8086/swagger-ui.html

## E2E tests (Node)

After stack is up:

```bash
npm install axios
node tests/e2e.js
```

## Troubleshooting

- **Gateway 503**: Wait for Eureka to have registered instances (check http://localhost:8761).
- **Auth/Wallet 401**: Use `Authorization: Bearer <token>` from `/api/auth/login`.
- **DB connection refused**: Ensure Postgres is healthy and `.env` has correct `POSTGRES_PASSWORD`.

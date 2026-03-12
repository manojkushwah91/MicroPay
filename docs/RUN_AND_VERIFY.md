# Run and Verify MicroPay

## Prerequisites

- Docker Desktop (with Docker Compose v2 or standalone docker-compose)
- For AWS: AWS CLI, ECR repository, EC2 or EKS

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
   - Frontend: http://localhost:3000
   - API Gateway: http://localhost:8080
   - Eureka: http://localhost:8761

## Production-style run (EC2/AWS-like)

```powershell
.\scripts\start-stack.ps1 -PostgresPassword "YourSecurePassword" -JwtSecret "YourJwtSecret"
```

Uses `docker-compose.prod.yml` (no Config Server; services use local config).

## E2E tests (Node)

After stack is up:

```bash
npm install axios
node tests/e2e.js
```

## AWS deployment

1. Set GitHub secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `EC2_HOST`, `EC2_USER`, `EC2_SSH_PRIVATE_KEY`, `POSTGRES_PASSWORD`, `JWT_SECRET`.
2. Push to `main`; workflow builds images, pushes to ECR, deploys to EC2.
3. Or run Terraform for EC2, then deploy with the same compose/prod flow.

## Troubleshooting

- **Gateway 503**: Wait for Eureka to have registered instances (check http://localhost:8761).
- **Auth/Wallet 401**: Use `Authorization: Bearer <token>` from `/api/auth/login`.
- **DB connection refused**: Ensure Postgres is healthy and `.env` has correct `POSTGRES_PASSWORD`.

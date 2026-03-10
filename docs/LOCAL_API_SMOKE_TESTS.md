## MicroPay Local API Smoke Tests

Assumes `docker compose -f infrastructure/docker/docker-compose.prod.yml --env-file .env up -d --build` is running and Nginx is listening on `http://localhost`.

### Auth

```bash
curl -i http://localhost/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@example.com",
    "password": "Password123!",
    "firstName": "Test",
    "lastName": "User"
  }'

curl -i http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@example.com",
    "password": "Password123!"
  }'
```

Copy the `token` and `userId` from the login response.

### Wallet

```bash
USER_ID=<uuid-from-login>
TOKEN=<jwt-from-login>

curl -i http://localhost/api/wallet/$USER_ID \
  -H "Authorization: Bearer $TOKEN"

curl -i http://localhost/api/wallet/$USER_ID/credit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000, "transactionId": "00000000-0000-0000-0000-000000000001"}'

curl -i http://localhost/api/wallet/$USER_ID/debit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100, "transactionId": "00000000-0000-0000-0000-000000000002"}'
```

### Payment

```bash
curl -i http://localhost/api/payment \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "payerUserId": "'$USER_ID'",
    "payeeUserId": "'$USER_ID'",
    "amount": 50,
    "currency": "INR",
    "reference": "test-payment"
  }'
```

### Transactions

```bash
curl -i http://localhost/api/transactions/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Notifications

```bash
curl -i "http://localhost/api/notifications/$USER_ID?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```



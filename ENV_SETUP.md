# Environment Variable Setup Guide

## Overview

This project now supports loading database credentials from a `.env` file using the `dotenv-java` library. This allows you to keep sensitive credentials out of your codebase and configuration files.

## Setup Instructions

### 1. Create a `.env` file

Create a `.env` file in the **project root** (`C:\MicroPay\.env`) with the following content:

```env
DB_PASSWORD=your_database_password_here
DB_USERNAME=postgres
DB_URL=jdbc:postgresql://localhost:5432/micropay_db
```

**Important:** 
- `DB_PASSWORD` is **required**. If it's missing, the application will fail to start with a clear error message.
- Replace `your_database_password_here` with your actual PostgreSQL database password.
- The `.env` file should be added to `.gitignore` to prevent committing sensitive credentials.

### 2. Verify the setup

When you start any service, you should see:
- A log message: `Successfully loaded environment variables from .env file`
- If `DB_PASSWORD` is missing, you'll see a clear error message with instructions

## How It Works

### DotEnvConfig Class

Each service has a `DotEnvConfig` class that:
1. Loads the `.env` file before Spring Boot starts
2. Sets all `.env` variables as system properties
3. Validates that `DB_PASSWORD` is present
4. Provides clear error messages if validation fails

### Application Properties

Each service's `application.properties` file uses:
```properties
spring.datasource.password=${DB_PASSWORD}
```

This means Spring Boot will:
1. First check system properties (set by DotEnvConfig from `.env`)
2. Then check environment variables
3. If `DB_PASSWORD` is missing, Spring Boot will fail with a clear error

## Services Updated

The following services now support `.env` file loading:

1. **auth-service** - `services/auth-service/`
2. **wallet-service** - `services/wallet-service/`
3. **payment-service** - `services/payment-service/`
4. **transaction-service** - `services/transaction-service/`
5. **notification-service** - `services/notification-service/`

## Files Changed

### Dependencies Added
- `dotenv-java` (version 3.0.0) added to all service `pom.xml` files

### New Files Created
- `DotEnvConfig.java` in each service's config package
- `application.properties` in each service's resources directory

### Files Modified
- All service main application classes now call `DotEnvConfig.loadDotEnv()` before starting Spring Boot

## Error Handling

If `DB_PASSWORD` is missing, you'll see:

```
================================================
CRITICAL: DB_PASSWORD is missing from .env file!
Please create a .env file in the project root with:
DB_PASSWORD=your_database_password
================================================
```

The application will then fail to start with:
```
IllegalStateException: DB_PASSWORD environment variable is required but not found in .env file. 
Please create a .env file in the project root with DB_PASSWORD set.
```

## Security Best Practices

1. **Never commit `.env` files** - Add `.env` to `.gitignore`
2. **Use strong passwords** - Use complex passwords for production databases
3. **Restrict file permissions** - On Unix systems, use `chmod 600 .env`
4. **Use different passwords** - Use different passwords for development, staging, and production

## Troubleshooting

### Issue: "DB_PASSWORD is missing" error

**Solution:** 
- Ensure `.env` file exists in the project root
- Check that `DB_PASSWORD=your_password` is in the file
- Verify there are no extra spaces or quotes around the password

### Issue: Application still can't connect to database

**Solution:**
- Verify the password in `.env` matches your PostgreSQL password
- Check that `DB_URL` points to the correct database
- Ensure PostgreSQL is running and accessible

### Issue: Variables not loading

**Solution:**
- Ensure `.env` file is in the project root (same directory as `pom.xml` files)
- Check file encoding (should be UTF-8)
- Verify no syntax errors in `.env` file (no spaces around `=`)

## Database Configuration

The datasource configuration supports:
- **Connection pooling** via HikariCP
- **JPA/Hibernate** for ORM
- **Flyway** for database migrations
- **PostgreSQL** as the database

All configurations are validated and will show clear errors if misconfigured.




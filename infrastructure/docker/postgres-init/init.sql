-- MicroPay Infrastructure Database Initialization
-- This script runs only once when the container is first created.

-- Create Application Databases
CREATE DATABASE micropay_auth_db;
CREATE DATABASE micropay_user_db;
CREATE DATABASE micropay_wallet_db;
CREATE DATABASE micropay_payment_db;
CREATE DATABASE micropay_transaction_db;
CREATE DATABASE micropay_notification_db;

-- Optional: If you want to ensure a specific user has full rights (Production Best Practice)
-- The environment variable POSTGRES_USER from your .env usually has these rights, 
-- but explicitly granting them ensures Flyway never hits a permission error.
GRANT ALL PRIVILEGES ON DATABASE micropay_auth_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE micropay_user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE micropay_wallet_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE micropay_payment_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE micropay_transaction_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE micropay_notification_db TO postgres;
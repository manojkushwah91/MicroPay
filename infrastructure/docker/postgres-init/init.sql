-- Create databases safely
SELECT 'CREATE DATABASE micropay_auth_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'micropay_auth_db')\gexec

SELECT 'CREATE DATABASE micropay_user_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'micropay_user_db')\gexec

SELECT 'CREATE DATABASE micropay_wallet_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'micropay_wallet_db')\gexec

SELECT 'CREATE DATABASE micropay_payment_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'micropay_payment_db')\gexec

SELECT 'CREATE DATABASE micropay_transaction_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'micropay_transaction_db')\gexec

SELECT 'CREATE DATABASE micropay_notification_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'micropay_notification_db')\gexec
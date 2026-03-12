# Encryption Tool

This tool is used to encrypt sensitive values for the MicroPay application.

## Prerequisites

- Jasypt CLI installed
- `JASYPT_ENCRYPTOR_PASSWORD` environment variable set to the encryption password.

## Usage

1.  Run the `encrypt-values.sh` script:

    ```bash
    ./encrypt-values.sh
    ```

2.  The script will output the encrypted values for `POSTGRES_PASSWORD` and `JWT_SECRET`.

3.  Replace the plaintext values in `micropay-secrets.yaml` with the encrypted values, wrapping them in `ENC()`:

    ```yaml
    stringData:
      POSTGRES_PASSWORD: ENC(encrypted-password)
      JWT_SECRET: ENC(encrypted-jwt-secret)
    ```

#!/bin/bash

# Check if Jasypt CLI is installed
if ! command -v jasypt.sh &> /dev/null
then
    echo "Jasypt CLI could not be found. Please install it first."
    exit
fi

# Check for password
if [ -z "$JASYPT_ENCRYPTOR_PASSWORD" ]; then
    echo "Please set the JASYPT_ENCRYPTOR_PASSWORD environment variable."
    exit
fi

# Encrypt values
echo "Encrypting values..."
echo "POSTGRES_PASSWORD: $(jasypt.sh encrypt input=micropay_pass password=$JASYPT_ENCRYPTOR_PASSWORD algorithm=PBEWithMD5AndDES)"
echo "JWT_SECRET: $(jasypt.sh encrypt input=micropay_jwt_secret_key_123456 password=$JASYPT_ENCRYPTOR_PASSWORD algorithm=PBEWithMD5AndDES)"

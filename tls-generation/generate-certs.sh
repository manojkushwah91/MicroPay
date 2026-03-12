#!/bin/bash

# Create a root CA
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -keyout rootCA.key -out rootCA.crt -subj "/C=US/ST=CA/L=San Francisco/O=MicroPay/OU=IT/CN=micropay.com"

# Create a keystore for each service
for service in api-gateway auth-service wallet-service payment-service transaction-service notification-service user-service eureka-server
do
  # Create a key and certificate for the service
  openssl req -new -nodes -newkey rsa:2048 -keyout $service.key -out $service.csr -subj "/C=US/ST=CA/L=San Francisco/O=MicroPay/OU=IT/CN=$service"
  openssl x509 -req -in $service.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out $service.crt -days 365 -sha256

  # Create a PKCS12 keystore
  openssl pkcs12 -export -out $service.p12 -name $service -inkey $service.key -in $service.crt -password pass:password

done

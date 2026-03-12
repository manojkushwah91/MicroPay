# TLS Certificate Generation

This script generates self-signed TLS certificates for each microservice.

## Usage

1.  Run the `generate-certs.sh` script:

    ```bash
    ./generate-certs.sh
    ```

2.  This will create a `rootCA.crt` file and a `.p12` file for each service.

3.  Copy the `rootCA.crt` file and the respective `.p12` file to the `src/main/resources` directory of each service.

4.  Update the `application.yml` file for each service to include the following configuration:

    ```yaml
    server:
      ssl:
        key-store-type: PKCS12
        key-store: classpath:<service-name>.p12
        key-store-password: password
        key-alias: <service-name>
    ```

5.  Update the Eureka client configuration in each service's `application.yml` to use HTTPS:

    ```yaml
    eureka:
      instance:
        secure-port-enabled: true
        non-secure-port-enabled: false
        home-page-url: https://${eureka.instance.hostname}:${server.port}/
        status-page-url: https://${eureka.instance.hostname}:${server.port}/actuator/info
        health-check-url: https://${eureka.instance.hostname}:${server.port}/actuator/health
      client:
        service-url:
          defaultZone: https://eureka-server:8761/eureka/
    ```

6.  Update the Eureka server's `application.yml` to enable TLS:

    ```yaml
    server:
      ssl:
        enabled: true
        key-store: classpath:eureka-server.p12
        key-store-password: password
        key-store-type: PKCS12
        key-alias: eureka-server
    ```

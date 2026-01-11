# MicroPay Kafka & Zookeeper Setup

Production-ready Docker Compose configuration for Apache Kafka and Zookeeper.

## Overview

This Docker Compose setup provides:
- **Zookeeper**: Cluster coordination and metadata management
- **Kafka**: Distributed event streaming platform
- **Automatic Topic Creation**: All required topics are created on startup

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB RAM available
- At least 10GB disk space

## Quick Start

### Start Services

```bash
cd infrastructure/docker
docker-compose -f docker-compose.kafka.yml up -d
```

### Stop Services

```bash
docker-compose -f docker-compose.kafka.yml down
```

### Stop and Remove Volumes

```bash
docker-compose -f docker-compose.kafka.yml down -v
```

## Services

### Zookeeper

- **Container**: `zookeeper`
- **Port**: `2181`
- **Image**: `confluentinc/cp-zookeeper:7.6.1`
- **Purpose**: Cluster coordination, metadata management, leader election

**Access**:
```bash
# Connect to Zookeeper
docker exec -it zookeeper zkCli.sh
```

### Kafka Broker

- **Container**: `kafka`
- **Port**: `9092` (external), `29092` (internal)
- **Image**: `confluentinc/cp-kafka:7.6.1`
- **Purpose**: Message broker, event streaming

**Access**:
```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe a topic
docker exec -it kafka kafka-topics --describe --topic payment.initiated --bootstrap-server localhost:9092

# Create a consumer
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.initiated --from-beginning

# Create a producer
docker exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic payment.initiated
```

### Kafka Init

- **Container**: `kafka-init`
- **Purpose**: Creates all required topics on startup
- **Runs**: Once on startup, then exits

## Topics

The following topics are automatically created:

### User Domain
- `user.created` - User registration events

### Wallet Domain
- `wallet.created` - Wallet creation events
- `wallet.balance.updated` - Balance update events

### Payment Domain
- `payment.initiated` - Payment initiation events
- `payment.authorized` - Payment authorization events
- `payment.completed` - Payment completion events
- `payment.failed` - Payment failure events

### Transaction Domain
- `transaction.recorded` - Transaction recording events

### Notification Domain
- `notification.send` - Notification sending events

### Topic Configuration

All topics are configured with:
- **Partitions**: 3 (for parallel processing)
- **Replication Factor**: 1 (single broker setup)
- **Retention**: 7 days (168 hours) for most topics, 1 day for notifications
- **Cleanup Policy**: Delete (old messages are deleted)

## Configuration

### Kafka Configuration

- **Auto-create Topics**: Disabled (topics must be created explicitly)
- **Compression**: Snappy (for better performance)
- **Log Retention**: 7 days (168 hours)
- **Segment Size**: 1GB

### Zookeeper Configuration

- **Tick Time**: 2000ms
- **Client Port**: 2181
- **Max Connections**: 60

## Connecting from Applications

### Spring Boot Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serialization.JsonSerializer
    consumer:
      group-id: your-service-consumer-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serialization.JsonDeserializer
```

### Docker Network Connection

If connecting from another Docker container:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:29092  # Use internal port
```

## Monitoring

### Check Service Status

```bash
# Check all services
docker-compose -f docker-compose.kafka.yml ps

# Check Kafka logs
docker logs kafka

# Check Zookeeper logs
docker logs zookeeper
```

### Health Checks

Both services include health checks:
- **Zookeeper**: Checks port 2181
- **Kafka**: Uses `kafka-broker-api-versions` command

## Adding New Topics

To add new topics, edit the `kafka-init` service in `docker-compose.kafka.yml`:

```yaml
kafka-topics --create --if-not-exists \
  --bootstrap-server kafka:29092 \
  --topic your.new.topic \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config cleanup.policy=delete
```

Then restart the services:

```bash
docker-compose -f docker-compose.kafka.yml restart kafka-init
```

## Production Considerations

### Current Setup (Development)

- Single Kafka broker
- Single Zookeeper instance
- Replication factor: 1
- No authentication/authorization
- Plaintext protocol

### Production Recommendations

1. **Multiple Brokers**: Deploy 3+ Kafka brokers for high availability
2. **Replication Factor**: Set to 3 for production topics
3. **Authentication**: Enable SASL/SSL authentication
4. **Authorization**: Configure ACLs for topic access control
5. **Monitoring**: Integrate with Prometheus and Grafana
6. **Backup**: Regular backup of Zookeeper and Kafka data
7. **Resource Limits**: Set appropriate CPU and memory limits
8. **Network Security**: Use private networks and firewalls

### Example Production Configuration

```yaml
kafka:
  environment:
    KAFKA_NUM_PARTITIONS: 6
    KAFKA_DEFAULT_REPLICATION_FACTOR: 3
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: SASL_SSL:SASL_SSL
    KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
    KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL: PLAIN
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 4G
      reservations:
        cpus: '1'
        memory: 2G
```

## Troubleshooting

### Topics Not Created

1. Check kafka-init logs:
   ```bash
   docker logs kafka-init
   ```

2. Verify Kafka is healthy:
   ```bash
   docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
   ```

3. Manually create topics if needed:
   ```bash
   docker exec -it kafka kafka-topics --create --bootstrap-server localhost:9092 --topic your-topic --partitions 3 --replication-factor 1
   ```

### Connection Issues

1. Verify services are running:
   ```bash
   docker-compose -f docker-compose.kafka.yml ps
   ```

2. Check network connectivity:
   ```bash
   docker network inspect micropay-network
   ```

3. Test connection from host:
   ```bash
   telnet localhost 9092
   ```

### Performance Issues

1. Check disk space:
   ```bash
   docker system df
   ```

2. Monitor resource usage:
   ```bash
   docker stats kafka zookeeper
   ```

3. Review logs for errors:
   ```bash
   docker logs kafka --tail 100
   ```

## Cleanup

### Remove All Data

```bash
# Stop and remove containers, networks, and volumes
docker-compose -f docker-compose.kafka.yml down -v

# Remove volumes manually if needed
docker volume rm micropay-zookeeper-data micropay-zookeeper-logs micropay-kafka-data
```

## License

Copyright © 2024 MicroPay. All rights reserved.


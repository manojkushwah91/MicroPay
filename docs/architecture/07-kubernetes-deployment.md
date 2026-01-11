# Kubernetes Deployment Strategy

## Cluster Architecture

### Cluster Topology

**Production**:
- **Primary Cluster**: 3 master nodes, 10+ worker nodes
- **Region**: Multi-AZ (Availability Zones)
- **Node Types**: 
  - General purpose: 8 CPU, 32GB RAM
  - Compute optimized: 16 CPU, 64GB RAM (for Payment, Transaction services)

**Staging**:
- **Cluster**: 1 master node, 5 worker nodes
- **Region**: Single AZ
- **Node Types**: General purpose: 4 CPU, 16GB RAM

**Development**:
- **Cluster**: 1 master node, 3 worker nodes
- **Node Types**: General purpose: 2 CPU, 8GB RAM

---

## Namespace Strategy

### Namespace Organization

```
micropay-production/
  ├── micropay-core/          # Core services (API Gateway, Config, Eureka)
  ├── micropay-user/          # User Service
  ├── micropay-account/       # Account Service
  ├── micropay-payment/       # Payment Service
  ├── micropay-transaction/   # Transaction Service
  ├── micropay-balance/       # Balance Service
  ├── micropay-notification/  # Notification Service
  ├── micropay-fraud/         # Fraud Detection Service
  ├── micropay-audit/         # Audit Service
  ├── micropay-reporting/     # Reporting Service
  └── micropay-infra/         # Infrastructure (Kafka, PostgreSQL, Redis)

micropay-staging/
  └── (Same structure as production)

micropay-development/
  └── (Same structure as production)
```

### Namespace Isolation

- **Network Policies**: Restrict inter-namespace communication
- **Resource Quotas**: Per-namespace resource limits
- **RBAC**: Namespace-scoped service accounts

---

## Deployment Strategy

### Deployment Types

#### 1. Rolling Update (Default)

**Services**: User, Account, Notification, Audit, Reporting

**Configuration**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: micropay-user
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
        version: v1.0.0
    spec:
      containers:
      - name: user-service
        image: registry.micropay.com/user-service:1.0.0
        ports:
        - containerPort: 8081
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2
            memory: 4Gi
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE
          value: "http://eureka-server:8761/eureka"
        - name: SPRING_CLOUD_CONFIG_URI
          value: "http://config-server:8888"
```

#### 2. Blue-Green Deployment

**Services**: Payment Service, Transaction Service

**Configuration**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-blue
  namespace: micropay-payment
spec:
  replicas: 5
  selector:
    matchLabels:
      app: payment-service
      version: blue
  template:
    metadata:
      labels:
        app: payment-service
        version: blue
    spec:
      containers:
      - name: payment-service
        image: registry.micropay.com/payment-service:1.0.0
        # ... container config
---
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: micropay-payment
spec:
  selector:
    app: payment-service
    version: blue  # Switch to 'green' for new version
  ports:
  - port: 8083
    targetPort: 8083
```

**Deployment Process**:
1. Deploy green version alongside blue
2. Run smoke tests on green
3. Switch service selector to green
4. Monitor green for 10 minutes
5. Scale down blue if green is healthy

#### 3. Canary Deployment

**Services**: API Gateway, Balance Service

**Configuration**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway-canary
  namespace: micropay-core
spec:
  replicas: 1  # 10% of total traffic
  selector:
    matchLabels:
      app: api-gateway
      version: canary
  template:
    metadata:
      labels:
        app: api-gateway
        version: canary
    spec:
      containers:
      - name: api-gateway
        image: registry.micropay.com/api-gateway:1.1.0
        # ... container config
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  selector:
    app: api-gateway
  ports:
  - port: 8080
    targetPort: 8080
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: api-gateway
spec:
  hosts:
  - api-gateway
  http:
  - match:
    - headers:
        canary:
          exact: "true"
    route:
    - destination:
        host: api-gateway
        subset: canary
      weight: 100
  - route:
    - destination:
        host: api-gateway
        subset: stable
      weight: 90
    - destination:
        host: api-gateway
        subset: canary
      weight: 10
```

**Deployment Process**:
1. Deploy canary (10% traffic)
2. Monitor metrics for 30 minutes
3. If healthy: Increase to 50%
4. Monitor for 30 minutes
5. If healthy: Increase to 100%
6. Remove old version

---

## Resource Management

### Resource Requests and Limits

| Service | CPU Request | Memory Request | CPU Limit | Memory Limit |
|---------|-------------|----------------|-----------|--------------|
| API Gateway | 500m | 1Gi | 2 | 4Gi |
| User Service | 500m | 1Gi | 2 | 4Gi |
| Account Service | 500m | 1Gi | 2 | 4Gi |
| Payment Service | 1000m | 2Gi | 4 | 8Gi |
| Transaction Service | 500m | 1Gi | 2 | 4Gi |
| Balance Service | 1000m | 2Gi | 4 | 8Gi |
| Notification Service | 500m | 1Gi | 2 | 4Gi |
| Fraud Detection | 1000m | 2Gi | 4 | 8Gi |
| Audit Service | 500m | 1Gi | 2 | 4Gi |
| Reporting Service | 500m | 1Gi | 2 | 4Gi |
| Config Server | 200m | 512Mi | 1 | 2Gi |
| Eureka Server | 200m | 512Mi | 1 | 2Gi |

### Resource Quotas

**Per Namespace**:
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: micropay-payment-quota
  namespace: micropay-payment
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    persistentvolumeclaims: "10"
    services: "10"
    pods: "20"
```

### Limit Ranges

**Per Namespace**:
```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: micropay-payment-limits
  namespace: micropay-payment
spec:
  limits:
  - default:
      cpu: "2"
      memory: 4Gi
    defaultRequest:
      cpu: "500m"
      memory: 1Gi
    type: Container
```

---

## Scaling Strategy

### Horizontal Pod Autoscaling (HPA)

**Services**: All services (except Config Server, Eureka)

**Configuration**:
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: payment-service-hpa
  namespace: micropay-payment
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: payment-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max
```

**Scaling Metrics**:
- **CPU**: Target 70% utilization
- **Memory**: Target 80% utilization
- **Custom Metrics**: Request rate, Kafka consumer lag

### Vertical Pod Autoscaling (VPA)

**Services**: Services with predictable resource usage

**Configuration**:
```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: user-service-vpa
  namespace: micropay-user
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: user-service
      minAllowed:
        cpu: 200m
        memory: 512Mi
      maxAllowed:
        cpu: 4
        memory: 8Gi
```

### Cluster Autoscaling

**Configuration**:
- **Min Nodes**: 5
- **Max Nodes**: 20
- **Scale Down**: After 10 minutes of low utilization
- **Scale Up**: Immediately when pods are pending

---

## Service Discovery & Load Balancing

### Service Configuration

**Example**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: micropay-payment
  labels:
    app: payment-service
spec:
  type: ClusterIP
  selector:
    app: payment-service
  ports:
  - name: http
    port: 8083
    targetPort: 8083
    protocol: TCP
  sessionAffinity: None
```

### Ingress Configuration

**API Gateway Ingress**:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-gateway-ingress
  namespace: micropay-core
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - api.micropay.com
    secretName: api-gateway-tls
  rules:
  - host: api.micropay.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: api-gateway
            port:
              number: 8080
```

---

## Configuration Management

### ConfigMaps

**Application Configuration**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-service-config
  namespace: micropay-payment
data:
  application.yml: |
    spring:
      application:
        name: payment-service
      kafka:
        bootstrap-servers: kafka:9092
    server:
      port: 8083
```

### Secrets

**Database Credentials**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: payment-service-secrets
  namespace: micropay-payment
type: Opaque
stringData:
  db-username: payment_user
  db-password: <encrypted-password>
  kafka-username: payment_service
  kafka-password: <encrypted-password>
```

**Secret Management**: External Secrets Operator (integrates with AWS Secrets Manager, HashiCorp Vault)

### External Configuration

**Spring Cloud Config Server**:
- Config Server deployed in Kubernetes
- Configuration stored in Git repository
- Dynamic refresh via webhook

---

## Storage Strategy

### Persistent Volumes

**Database Storage**:
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: payment-db-pvc
  namespace: micropay-payment
spec:
  accessModes:
  - ReadWriteOnce
  storageClassName: fast-ssd
  resources:
    requests:
      storage: 100Gi
```

**Storage Classes**:
- **fast-ssd**: For databases (SSD, high IOPS)
- **standard**: For logs and backups (HDD, standard IOPS)

### Backup Strategy

**Database Backups**:
- **Tool**: Velero or custom backup jobs
- **Frequency**: Daily at 2 AM UTC
- **Retention**: 30 days
- **Storage**: S3 or Azure Blob Storage

---

## Monitoring & Observability

### Service Mesh (Optional)

**Technology**: Istio or Linkerd

**Benefits**:
- mTLS between services
- Traffic management
- Observability (metrics, traces, logs)
- Circuit breakers

### Prometheus Integration

**ServiceMonitor**:
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: payment-service-monitor
  namespace: micropay-payment
spec:
  selector:
    matchLabels:
      app: payment-service
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

### Logging

**Fluentd/Fluent Bit**: Collect logs from all pods
**Elasticsearch**: Store logs
**Kibana**: Visualize logs

---

## Network Policies

### Network Isolation

**Example Policy**:
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: payment-service-policy
  namespace: micropay-payment
spec:
  podSelector:
    matchLabels:
      app: payment-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: micropay-core
    - podSelector:
        matchLabels:
          app: api-gateway
    ports:
    - protocol: TCP
      port: 8083
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: micropay-infra
    - podSelector:
        matchLabels:
          app: kafka
    ports:
  - protocol: TCP
      port: 9092
  - to:
    - namespaceSelector:
        matchLabels:
          name: micropay-infra
    - podSelector:
        matchLabels:
          app: postgresql
    ports:
    - protocol: TCP
      port: 5432
```

---

## Health Checks

### Liveness Probe

**Purpose**: Restart pod if application is deadlocked

**Configuration**:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8083
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe

**Purpose**: Remove pod from service if not ready

**Configuration**:
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8083
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

### Startup Probe

**Purpose**: Allow slow-starting applications more time

**Configuration**:
```yaml
startupProbe:
  httpGet:
    path: /actuator/health
    port: 8083
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 30  # Allow up to 150 seconds for startup
```

---

## Disaster Recovery

### Multi-Region Deployment

**Primary Region**: us-east-1
**Secondary Region**: us-west-2

**Strategy**:
- Active-Passive: Primary handles all traffic, secondary on standby
- Database replication: Cross-region replication
- Failover: Manual or automated (via Route53 health checks)

### Backup & Restore

**Backup**:
- Database: Daily automated backups
- ConfigMaps/Secrets: Versioned in Git
- Persistent Volumes: Snapshot daily

**Restore**:
- RTO (Recovery Time Objective): 4 hours
- RPO (Recovery Point Objective): 24 hours

---

## Security

### Pod Security Policies

**Restricted Policy**:
```yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: micropay-restricted
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
  - ALL
  volumes:
  - 'configMap'
  - 'emptyDir'
  - 'projected'
  - 'secret'
  - 'downwardAPI'
  - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
```

### RBAC

**Service Account**:
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: payment-service-sa
  namespace: micropay-payment
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: payment-service-role
  namespace: micropay-payment
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: payment-service-rolebinding
  namespace: micropay-payment
subjects:
- kind: ServiceAccount
  name: payment-service-sa
  namespace: micropay-payment
roleRef:
  kind: Role
  name: payment-service-role
  apiGroup: rbac.authorization.k8s.io
```

---

## Deployment Automation

### GitOps (ArgoCD/Flux)

**Strategy**: Declarative GitOps
- Kubernetes manifests in Git
- ArgoCD syncs Git to cluster
- Automated deployments on Git push

**Workflow**:
1. Developer pushes code
2. CI/CD builds and pushes Docker image
3. Developer updates Kubernetes manifest (image tag)
4. ArgoCD detects change and deploys

### Helm Charts

**Chart Structure**:
```
charts/
  ├── micropay-services/
  │   ├── Chart.yaml
  │   ├── values.yaml
  │   └── templates/
  │       ├── deployment.yaml
  │       ├── service.yaml
  │       ├── configmap.yaml
  │       └── hpa.yaml
```

**Deployment**:
```bash
helm install payment-service ./charts/micropay-services \
  --namespace micropay-payment \
  --set image.tag=1.0.0 \
  --set replicas=3
```


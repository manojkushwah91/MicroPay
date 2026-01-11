# CI/CD Pipeline Design

## Pipeline Overview

**CI/CD Tool**: Jenkins  
**Artifact Repository**: Nexus  
**Code Quality**: SonarQube  
**Container Registry**: Docker Registry (or AWS ECR / Azure ACR)

## Pipeline Architecture

### Multi-Branch Pipeline Strategy

Each service has its own Jenkins pipeline:
- `user-service-pipeline`
- `account-service-pipeline`
- `payment-service-pipeline`
- `transaction-service-pipeline`
- `balance-service-pipeline`
- `notification-service-pipeline`
- `fraud-detection-service-pipeline`
- `audit-service-pipeline`
- `reporting-service-pipeline`
- `api-gateway-pipeline`
- `config-server-pipeline`
- `eureka-server-pipeline`

### Branch Strategy

- **main**: Production-ready code
- **develop**: Integration branch
- **feature/***: Feature branches
- **hotfix/***: Production hotfixes
- **release/***: Release candidates

---

## Pipeline Stages

### Stage 1: Checkout & Preparation

**Actions**:
1. Checkout source code from Git
2. Set build number and version
3. Create build metadata
4. Initialize workspace

**Jenkinsfile**:
```groovy
stage('Checkout') {
    steps {
        checkout scm
        script {
            env.BUILD_NUMBER = "${env.BUILD_NUMBER}"
            env.VERSION = "${readMavenPom().version}"
            env.SERVICE_NAME = "${SERVICE_NAME}"
        }
    }
}
```

**Outputs**:
- Source code in workspace
- Build metadata (version, commit hash, branch)

---

### Stage 2: Dependency Resolution

**Actions**:
1. Download Maven dependencies
2. Cache dependencies (for faster builds)
3. Validate dependency versions

**Jenkinsfile**:
```groovy
stage('Dependencies') {
    steps {
        sh 'mvn dependency:resolve -U'
        sh 'mvn dependency:tree > dependency-tree.txt'
    }
    post {
        always {
            archiveArtifacts artifacts: 'dependency-tree.txt', fingerprint: true
        }
    }
}
```

**Cache Strategy**:
- Maven local repository cached in Jenkins workspace
- Cache key: `maven-deps-${SERVICE_NAME}`

---

### Stage 3: Code Quality Analysis

**Tool**: SonarQube

**Actions**:
1. Run SonarQube analysis
2. Check code coverage
3. Validate code quality gates
4. Generate quality report

**Jenkinsfile**:
```groovy
stage('Code Quality') {
    steps {
        withSonarQubeEnv('SonarQube') {
            sh """
                mvn clean verify sonar:sonar \
                    -Dsonar.projectKey=${SERVICE_NAME} \
                    -Dsonar.projectName=${SERVICE_NAME} \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
            """
        }
    }
}
```

**Quality Gates**:
- Code Coverage: ≥ 70%
- Duplicated Lines: ≤ 3%
- Maintainability Rating: A
- Reliability Rating: A
- Security Rating: A
- Technical Debt Ratio: ≤ 5%

**Failure Action**: Block merge if quality gates fail

---

### Stage 4: Unit Tests

**Actions**:
1. Run unit tests
2. Generate test reports
3. Calculate code coverage
4. Archive test results

**Jenkinsfile**:
```groovy
stage('Unit Tests') {
    steps {
        sh 'mvn test -DskipITs'
    }
    post {
        always {
            junit 'target/surefire-reports/*.xml'
            jacoco(
                execPattern: 'target/jacoco.exec',
                classPattern: 'target/classes',
                sourcePattern: 'src/main/java'
            )
        }
        failure {
            emailext (
                subject: "Unit Tests Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Unit tests failed. Check console output.",
                to: "${TEAM_EMAIL}"
            )
        }
    }
}
```

**Coverage Tool**: JaCoCo

**Minimum Coverage**: 70%

**Failure Action**: Fail build if coverage < 70%

---

### Stage 5: Integration Tests

**Actions**:
1. Start test containers (PostgreSQL, Redis, Kafka)
2. Run integration tests
3. Generate integration test reports
4. Cleanup test containers

**Jenkinsfile**:
```groovy
stage('Integration Tests') {
    steps {
        sh '''
            docker-compose -f docker-compose.test.yml up -d
            sleep 30
            mvn verify -DskipUTs
        '''
    }
    post {
        always {
            junit 'target/failsafe-reports/*.xml'
            sh 'docker-compose -f docker-compose.test.yml down -v'
        }
    }
}
```

**Test Containers**:
- Testcontainers (Java library)
- PostgreSQL container
- Redis container
- Kafka container (via Testcontainers)

---

### Stage 6: Build Artifact

**Actions**:
1. Compile source code
2. Run package goal (create JAR)
3. Sign artifact (optional, for security)
4. Generate build manifest

**Jenkinsfile**:
```groovy
stage('Build') {
    steps {
        sh 'mvn clean package -DskipTests'
        script {
            env.ARTIFACT_PATH = "target/${SERVICE_NAME}-${VERSION}.jar"
        }
    }
    post {
        success {
            archiveArtifacts artifacts: "${ARTIFACT_PATH}", fingerprint: true
        }
    }
}
```

**Artifact Naming**:
- Format: `{service-name}-{version}.jar`
- Example: `payment-service-1.0.0.jar`

---

### Stage 7: Security Scanning

**Tools**: OWASP Dependency Check, Snyk

**Actions**:
1. Scan dependencies for vulnerabilities
2. Scan Docker image for vulnerabilities
3. Generate security report
4. Check against security policy

**Jenkinsfile**:
```groovy
stage('Security Scan') {
    steps {
        sh 'mvn org.owasp:dependency-check-maven:check'
        sh 'snyk test --severity-threshold=high'
    }
    post {
        always {
            archiveArtifacts artifacts: 'dependency-check-report.html', fingerprint: true
        }
    }
}
```

**Security Policy**:
- Critical vulnerabilities: Block build
- High vulnerabilities: Block build
- Medium vulnerabilities: Warn, allow build
- Low vulnerabilities: Informational

---

### Stage 8: Build Docker Image

**Actions**:
1. Build Docker image
2. Tag image with version and build number
3. Scan Docker image
4. Push to container registry

**Jenkinsfile**:
```groovy
stage('Docker Build') {
    steps {
        script {
            def imageName = "${DOCKER_REGISTRY}/${SERVICE_NAME}:${VERSION}"
            def imageTag = "${DOCKER_REGISTRY}/${SERVICE_NAME}:${BUILD_NUMBER}"
            def imageLatest = "${DOCKER_REGISTRY}/${SERVICE_NAME}:latest"
            
            sh """
                docker build -t ${imageName} -t ${imageTag} -t ${imageLatest} .
                docker push ${imageName}
                docker push ${imageTag}
                if [ "${env.BRANCH_NAME}" == "main" ]; then
                    docker push ${imageLatest}
                fi
            """
        }
    }
}
```

**Docker Image Tags**:
- `{version}`: Semantic version (e.g., `1.0.0`)
- `{build-number}`: Jenkins build number (e.g., `123`)
- `latest`: Only for `main` branch

**Image Scanning**: Trivy or Clair

---

### Stage 9: Publish to Nexus

**Actions**:
1. Upload JAR to Nexus Maven repository
2. Upload Docker image to Nexus Docker registry
3. Update repository metadata

**Jenkinsfile**:
```groovy
stage('Publish to Nexus') {
    steps {
        script {
            nexusArtifactUploader(
                nexusVersion: 'nexus3',
                protocol: 'https',
                nexusUrl: "${NEXUS_URL}",
                groupId: 'com.micropay',
                version: "${VERSION}",
                repository: "${REPOSITORY_NAME}",
                credentialsId: 'nexus-credentials',
                artifacts: [
                    [artifactId: "${SERVICE_NAME}",
                     classifier: '',
                     file: "${ARTIFACT_PATH}",
                     type: 'jar']
                ]
            )
        }
    }
}
```

**Repository Structure**:
```
com.micropay/
  ├── user-service/
  ├── account-service/
  ├── payment-service/
  └── ...
```

---

### Stage 10: Deploy to Staging

**Condition**: Only for `develop` and `release/*` branches

**Actions**:
1. Update Kubernetes manifests with new image tag
2. Deploy to staging namespace
3. Run smoke tests
4. Verify deployment health

**Jenkinsfile**:
```groovy
stage('Deploy to Staging') {
    when {
        anyOf {
            branch 'develop'
            branch 'release/*'
        }
    }
    steps {
        script {
            sh """
                kubectl set image deployment/${SERVICE_NAME} \
                    ${SERVICE_NAME}=${DOCKER_REGISTRY}/${SERVICE_NAME}:${BUILD_NUMBER} \
                    -n micropay-staging
                kubectl rollout status deployment/${SERVICE_NAME} -n micropay-staging
            """
        }
    }
    post {
        success {
            sh './scripts/smoke-tests.sh staging'
        }
    }
}
```

**Smoke Tests**:
- Health check endpoint
- Basic API calls
- Database connectivity
- Kafka connectivity

---

### Stage 11: E2E Tests (Staging)

**Condition**: Only after successful staging deployment

**Actions**:
1. Run end-to-end tests against staging
2. Generate E2E test report
3. Validate critical user flows

**Jenkinsfile**:
```groovy
stage('E2E Tests') {
    when {
        anyOf {
            branch 'develop'
            branch 'release/*'
        }
    }
    steps {
        sh './e2e-tests/run-tests.sh staging'
    }
    post {
        always {
            publishHTML([
                reportDir: 'e2e-tests/reports',
                reportFiles: 'index.html',
                reportName: 'E2E Test Report'
            ])
        }
    }
}
```

**E2E Test Scenarios**:
- User registration and login
- Account creation
- Payment initiation and completion
- Transaction history
- Balance queries

---

### Stage 12: Deploy to Production

**Condition**: Only for `main` branch, manual approval required

**Actions**:
1. Manual approval gate
2. Update production Kubernetes manifests
3. Deploy to production namespace (blue-green or rolling update)
4. Run smoke tests
5. Monitor deployment metrics

**Jenkinsfile**:
```groovy
stage('Deploy to Production') {
    when {
        branch 'main'
    }
    steps {
        script {
            timeout(time: 1, unit: 'HOURS') {
                input message: 'Deploy to Production?',
                      ok: 'Deploy',
                      submitterParameter: 'APPROVER'
            }
            
            sh """
                kubectl set image deployment/${SERVICE_NAME} \
                    ${SERVICE_NAME}=${DOCKER_REGISTRY}/${SERVICE_NAME}:${VERSION} \
                    -n micropay-production
                kubectl rollout status deployment/${SERVICE_NAME} -n micropay-production --timeout=10m
            """
        }
    }
    post {
        success {
            sh './scripts/smoke-tests.sh production'
            sh './scripts/monitor-deployment.sh production'
        }
        failure {
            sh './scripts/rollback-deployment.sh production'
        }
    }
}
```

**Deployment Strategy**:
- **Blue-Green**: For critical services (Payment, Transaction)
- **Rolling Update**: For other services
- **Canary**: For high-risk changes (10% → 50% → 100%)

---

### Stage 13: Post-Deployment Verification

**Actions**:
1. Verify service health
2. Check metrics (error rate, latency)
3. Validate logs
4. Run regression tests

**Jenkinsfile**:
```groovy
stage('Post-Deployment Verification') {
    steps {
        sh './scripts/verify-deployment.sh'
        sh './scripts/check-metrics.sh'
    }
}
```

**Verification Checks**:
- Health endpoint returns 200
- Error rate < 0.1%
- P95 latency < 500ms
- No critical errors in logs

---

## Pipeline Triggers

### Automatic Triggers

1. **Push to Branch**: Trigger on every push
2. **Pull Request**: Trigger on PR creation/update
3. **Schedule**: Nightly builds for `develop` branch
4. **Webhook**: Trigger from Git webhook

### Manual Triggers

1. **Rebuild**: Re-run failed pipeline
2. **Deploy to Production**: Manual approval gate

---

## Pipeline Configuration Files

### Jenkinsfile Location

Each service has its own `Jenkinsfile`:
```
services/
  ├── user-service/
  │   └── Jenkinsfile
  ├── payment-service/
  │   └── Jenkinsfile
  └── ...
```

### Shared Libraries

**Location**: `jenkins-shared-libraries/`

**Libraries**:
- `micropay-pipeline`: Common pipeline steps
- `micropay-kubernetes`: Kubernetes deployment utilities
- `micropay-testing`: Test execution utilities

**Usage**:
```groovy
@Library('micropay-pipeline') _

pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                micropayBuild()
            }
        }
    }
}
```

---

## Environment-Specific Configuration

### Development
- **Auto-deploy**: Yes
- **Approval**: Not required
- **Tests**: Unit tests only

### Staging
- **Auto-deploy**: Yes (from `develop` branch)
- **Approval**: Not required
- **Tests**: Unit + Integration + E2E

### Production
- **Auto-deploy**: No
- **Approval**: Required (manual gate)
- **Tests**: All tests + smoke tests

---

## Rollback Strategy

### Automatic Rollback

**Triggers**:
- Health check failures (3 consecutive failures)
- Error rate > 5%
- Deployment timeout (> 10 minutes)

**Process**:
1. Detect failure
2. Rollback to previous version
3. Notify team
4. Create incident ticket

### Manual Rollback

**Command**:
```bash
kubectl rollout undo deployment/{service-name} -n micropay-production
```

**Process**:
1. Identify previous working version
2. Execute rollback command
3. Verify rollback success
4. Monitor metrics

---

## Notification Strategy

### Success Notifications
- **Slack**: Success message to team channel
- **Email**: Summary report (optional)

### Failure Notifications
- **Slack**: Alert to team channel + on-call engineer
- **Email**: Detailed failure report
- **PagerDuty**: Critical failures (production only)

### Notification Channels
- **Development**: Slack only
- **Staging**: Slack + Email
- **Production**: Slack + Email + PagerDuty

---

## Pipeline Metrics & Monitoring

### Key Metrics
- Build duration
- Build success rate
- Test execution time
- Code coverage trend
- Deployment frequency
- Mean time to recovery (MTTR)

### Dashboards
- **Jenkins Dashboard**: Build status, trends
- **Grafana Dashboard**: Pipeline metrics, deployment metrics

### Alerts
- Build failure rate > 10%
- Build duration > 30 minutes
- Deployment failure
- Security scan failures

---

## Best Practices

1. **Idempotent Builds**: Builds should be repeatable
2. **Fast Feedback**: Fail fast, provide clear error messages
3. **Artifact Versioning**: Use semantic versioning
4. **Immutable Artifacts**: Never modify artifacts after creation
5. **Infrastructure as Code**: Kubernetes manifests in Git
6. **Secrets Management**: Use Jenkins credentials or external secret manager
7. **Parallel Execution**: Run independent stages in parallel
8. **Cache Optimization**: Cache dependencies and Docker layers


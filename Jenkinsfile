pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml build'
            }
        }
        stage('Test') {
            steps {
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm api-gateway mvn test'
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm auth-service mvn test'
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm payment-service mvn test'
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm wallet-service mvn test'
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm transaction-service mvn test'
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm notification-service mvn test'
                sh 'docker-compose -f infrastructure/docker/docker-compose.prod.yml run --rm user-service mvn test'
            }
        }
    }
}

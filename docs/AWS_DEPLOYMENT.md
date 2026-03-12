# AWS Deployment Plan

This document describes how to deploy the MicroPay application to AWS.

## 1. Prerequisites

* An AWS account
* The AWS CLI installed and configured
* `kubectl` installed and configured
* `eksctl` installed and configured
* Docker installed

## 2. Infrastructure Setup

### 2.1. VPC

Create a new VPC with public and private subnets.

### 2.2. EKS Cluster

Create a new EKS cluster in the VPC.

```bash
eksctl create cluster --name micropay-cluster --region us-east-1 --nodegroup-name standard-workers --node-type t3.medium --nodes 3 --nodes-min 1 --nodes-max 4 --managed
```

### 2.3. RDS Database

Create a new PostgreSQL RDS instance in the VPC.

### 2.4. ElastiCache for Redis

Create a new ElastiCache for Redis cluster in the VPC.

### 2.5. ECR Registry

Create a new ECR registry to store the Docker images.

## 3. Application Deployment

### 3.1. Build and Push Docker Images

Build the Docker images for each service and push them to the ECR registry.

```bash
docker-compose -f infrastructure/docker/docker-compose.prod.yml build

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <aws_account_id>.dkr.ecr.us-east-1.amazonaws.com

docker tag micropay-api-gateway:latest <aws_account_id>.dkr.ecr.us-east-1.amazonaws.com/micropay-api-gateway:latest
docker push <aws_account_id>.dkr.ecr.us-east-1.amazonaws.com/micropay-api-gateway:latest

# Repeat for all services
```

### 3.2. Deploy to EKS

Create Kubernetes deployment and service files for each service. The `infrastructure/k8s` directory contains example files that can be used as a starting point.

Update the environment variables in the deployment files to point to the RDS database and ElastiCache for Redis instances.

Deploy the services to the EKS cluster.

```bash
kubectl apply -f infrastructure/k8s/
```

## 4. CI/CD

Create a CI/CD pipeline using Jenkins or another CI/CD tool. The pipeline should automate the build, test, and deployment process.

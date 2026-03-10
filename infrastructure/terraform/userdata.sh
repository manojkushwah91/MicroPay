#!/bin/bash
set -euo pipefail

dnf update -y
dnf install -y docker git unzip curl

systemctl enable --now docker
usermod -aG docker ec2-user || true

# Install Docker Compose plugin if missing
if ! docker compose version >/dev/null 2>&1; then
  mkdir -p /usr/local/lib/docker/cli-plugins
  COMPOSE_VERSION="v2.29.2"
  curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
  chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
fi

# Install AWS CLI v2 if missing
if ! aws --version >/dev/null 2>&1; then
  curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
  unzip -q /tmp/awscliv2.zip -d /tmp
  /tmp/aws/install
fi

mkdir -p /opt/micropay
chown -R ec2-user:ec2-user /opt/micropay


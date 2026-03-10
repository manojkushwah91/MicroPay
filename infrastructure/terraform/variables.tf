variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Name prefix for AWS resources"
  type        = string
  default     = "micropay"
}

variable "instance_type" {
  description = "EC2 instance type (Free Tier: t2.micro)"
  type        = string
  default     = "t2.micro"
}

variable "root_volume_gb" {
  description = "Root EBS volume size (GB)"
  type        = number
  default     = 20
}

variable "ssh_key_name" {
  description = "Existing EC2 key pair name for SSH"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to SSH to EC2 (lock this to your public IP)"
  type        = string
  default     = "0.0.0.0/0"
}

variable "ec2_name" {
  description = "EC2 instance Name tag"
  type        = string
  default     = "micropay-prod"
}

variable "frontend_domain_name" {
  description = "Optional custom domain for CloudFront aliases (leave blank to skip)"
  type        = string
  default     = ""
}


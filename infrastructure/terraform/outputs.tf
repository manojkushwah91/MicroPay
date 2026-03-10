output "ec2_public_ip" {
  value       = aws_instance.micropay.public_ip
  description = "Public IP of the MicroPay EC2 instance"
}

output "ec2_public_dns" {
  value       = aws_instance.micropay.public_dns
  description = "Public DNS of the MicroPay EC2 instance"
}

output "security_group_id" {
  value       = aws_security_group.micropay_sg.id
  description = "Security group ID"
}

output "ecr_repository_urls" {
  value       = { for k, v in aws_ecr_repository.repos : k => v.repository_url }
  description = "ECR repository URLs"
}

output "frontend_s3_bucket" {
  value       = aws_s3_bucket.frontend.bucket
  description = "S3 bucket name for frontend static site"
}

output "frontend_cloudfront_domain" {
  value       = aws_cloudfront_distribution.frontend.domain_name
  description = "CloudFront domain name for frontend"
}


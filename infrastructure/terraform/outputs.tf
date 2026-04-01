output "ec2_elastic_ip" {
  description = "Elastic IP address of the EC2 instance"
  value       = aws_eip.main.public_ip
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_address" {
  description = "RDS PostgreSQL address (hostname only)"
  value       = aws_db_instance.postgres.address
}

output "s3_bucket_name" {
  description = "S3 bucket name for application storage"
  value       = aws_s3_bucket.main.bucket
}

output "s3_bucket_arn" {
  description = "S3 bucket ARN"
  value       = aws_s3_bucket.main.arn
}

output "cloudfront_domain_name" {
  description = "Domain name for Client to read file"
  value       = aws_cloudfront_distribution.cdn.domain_name
}

output "s3_client_access_key_id" {
  description = "Access Key ID for Client"
  value       = aws_iam_access_key.s3_client_key.id
}

output "s3_client_secret_access_key" {
  description = "Secret Access Key for Client"
  value       = aws_iam_access_key.s3_client_key.secret
  sensitive   = true
}

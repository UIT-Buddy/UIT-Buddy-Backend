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

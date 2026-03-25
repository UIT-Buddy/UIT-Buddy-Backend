variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "ap-southeast-2"
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "uitbuddy"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_a_cidr" {
  description = "CIDR block for private subnet 1"
  type        = string
  default     = "10.0.2.0/24"
}

variable "private_subnet_b_cidr" {
  description = "CIDR block for private subnet 2 (for RDS DB subnet group)"
  type        = string
  default     = "10.0.3.0/24"
}

variable "az_a" {
  description = "Availability Zone A"
  type        = string
  default     = "ap-southeast-2a"
}

variable "az_b" {
  description = "Availability Zone B"
  type        = string
  default     = "ap-southeast-2b"
}

variable "postgres_db" {
  description = "PostgreSQL database name"
  type        = string
  default     = "uitbuddy"
}

variable "postgres_user" {
  description = "PostgreSQL username"
  type        = string
  default     = "admin"
}

variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_password" {
  description = "PostgreSQL password (stored in Secrets Manager)"
  type        = string
  sensitive   = true
}

variable "github_runner_token" {
  description = "GitHub Actions self-hosted runner registration token"
  type        = string
  sensitive   = true
}

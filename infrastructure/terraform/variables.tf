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

variable "public_subnet_a_cidr" {
  description = "CIDR block for public subnet A"
  type        = string
  default     = "10.0.1.0/24"
}

variable "public_subnet_b_cidr" {
  description = "CIDR block for public subnet B"
  type        = string
  default     = "10.0.2.0/24"
}

variable "private_subnet_a_cidr" {
  description = "CIDR block for private subnet A"
  type        = string
  default     = "10.0.11.0/24"
}

variable "private_subnet_b_cidr" {
  description = "CIDR block for private subnet B"
  type        = string
  default     = "10.0.12.0/24"
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

variable "backend_image" {
  description = "Docker image for the backend service"
  type        = string
  default     = "ghcr.io/uit-buddy/backend:latest"
}

variable "postgres_image" {
  description = "Docker image for PostgreSQL"
  type        = string
  default     = "docker.io/library/postgres:17-alpine"
}

variable "redis_image" {
  description = "Docker image for Redis"
  type        = string
  default     = "docker.io/library/redis:8-alpine"
}

variable "backend_cpu" {
  description = "CPU units for backend task (1024 = 1 vCPU)"
  type        = number
  default     = 1024
}

variable "backend_memory" {
  description = "Memory (MB) for backend task"
  type        = number
  default     = 2048
}

variable "postgres_cpu" {
  description = "CPU units for postgres task (1024 = 1 vCPU)"
  type        = number
  default     = 1024
}

variable "postgres_memory" {
  description = "Memory (MB) for postgres task"
  type        = number
  default     = 2048
}

variable "internal_dns_namespace" {
  description = "Internal DNS namespace for service discovery"
  type        = string
  default     = "uitbuddy.local"
}

variable "db_password" {
  description = "PostgreSQL password (stored in Secrets Manager)"
  type        = string
  sensitive   = true
}

variable "firebase_json" {
  description = "Firebase service account JSON content (stored in Secrets Manager)"
  type        = string
  sensitive   = true
}

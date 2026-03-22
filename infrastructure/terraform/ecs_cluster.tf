# ──────────────────────────────────────────────
# ECS Cluster
# ──────────────────────────────────────────────
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
}

# ──────────────────────────────────────────────
# Cloud Map — Private DNS Namespace for service discovery
# ──────────────────────────────────────────────
resource "aws_service_discovery_private_dns_namespace" "internal" {
  name        = var.internal_dns_namespace
  description = "Internal DNS for ${var.project_name} microservices"
  vpc         = aws_vpc.main.id
}

# ──────────────────────────────────────────────
# Cloud Map — Service Discovery for PostgreSQL
# Backend connects via: postgres.uitbuddy.local
# ──────────────────────────────────────────────
resource "aws_service_discovery_service" "postgres" {
  name = "postgres"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.internal.id
    dns_records {
      ttl  = 10
      type = "A"
    }
    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

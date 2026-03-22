# ──────────────────────────────────────────────
# CloudWatch Log Group — Backend
# ──────────────────────────────────────────────
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/${var.project_name}-backend"
  retention_in_days = 14
}

# ──────────────────────────────────────────────
# ECS Task Definition — Backend + Redis (Sidecar)
# ──────────────────────────────────────────────
resource "aws_ecs_task_definition" "backend" {
  family                   = "${var.project_name}-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.backend_cpu
  memory                   = var.backend_memory
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    # Container 1: Redis (Sidecar)
    {
      name      = "redis"
      image     = var.redis_image
      essential = true
      command   = ["redis-server", "--appendonly", "yes"]

      portMappings = [
        { containerPort = 6379, hostPort = 6379, protocol = "tcp" }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.backend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "redis"
        }
      }
    },
    # Container 2: Spring Boot Backend
    {
      name      = "backend"
      image     = var.backend_image
      essential = true

      portMappings = [
        { containerPort = 8080, hostPort = 8080, protocol = "tcp" }
      ]

      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://postgres.${var.internal_dns_namespace}:5432/${var.postgres_db}?connectTimeout=3" },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.postgres_user },
        { name = "DEV_REDIS_HOST", value = "127.0.0.1" },
        { name = "DEV_REDIS_PORT", value = "6379" }
      ]

      secrets = [
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.backend_secrets.arn}:POSTGRES_PASSWORD::"
        },
        {
          name      = "FIREBASE_JSON_CONFIG"
          valueFrom = "${aws_secretsmanager_secret.backend_secrets.arn}:FIREBASE_JSON::"
        }
      ]

      dependsOn = [
        { containerName = "redis", condition = "START" }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.backend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "backend"
        }
      }
    }
  ])
}

# ──────────────────────────────────────────────
# ECS Service — Backend (Public Subnet + ALB)
# ──────────────────────────────────────────────
resource "aws_ecs_service" "backend" {
  name            = "${var.project_name}-backend"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_a.id, aws_subnet.public_b.id]
    security_groups  = [aws_security_group.backend_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.backend.arn
    container_name   = "backend"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.http]
}

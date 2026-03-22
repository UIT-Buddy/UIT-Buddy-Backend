# ──────────────────────────────────────────────
# CloudWatch Log Group — PostgreSQL
# ──────────────────────────────────────────────
resource "aws_cloudwatch_log_group" "postgres" {
  name              = "/ecs/${var.project_name}-postgres"
  retention_in_days = 14
}

# ──────────────────────────────────────────────
# ECS Task Definition — PostgreSQL
# ──────────────────────────────────────────────
resource "aws_ecs_task_definition" "postgres" {
  family                   = "${var.project_name}-postgres"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.postgres_cpu
  memory                   = var.postgres_memory
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  volume {
    name = "postgres-storage"
    efs_volume_configuration {
      file_system_id = aws_efs_file_system.postgres_data.id
      root_directory = "/"
    }
  }

  container_definitions = jsonencode([
    {
      name      = "postgres"
      image     = var.postgres_image
      essential = true

      portMappings = [
        { containerPort = 5432, hostPort = 5432, protocol = "tcp" }
      ]

      environment = [
        { name = "POSTGRES_DB", value = var.postgres_db },
        { name = "POSTGRES_USER", value = var.postgres_user }
      ]

      secrets = [
        {
          name      = "POSTGRES_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.backend_secrets.arn}:POSTGRES_PASSWORD::"
        }
      ]

      mountPoints = [
        {
          sourceVolume  = "postgres-storage"
          containerPath = "/var/lib/postgresql/data"
          readOnly      = false
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.postgres.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "postgres"
        }
      }
    }
  ])
}

# ──────────────────────────────────────────────
# ECS Service — PostgreSQL (Private Subnet + Cloud Map)
# ──────────────────────────────────────────────
resource "aws_ecs_service" "postgres" {
  name            = "${var.project_name}-postgres"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.postgres.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  # Bật ECS Exec — cho phép chui vào container qua SSM (thay thế SSH)
  enable_execute_command = true

  network_configuration {
    # Public Subnet để có thể pull image từ Docker Hub và đọc Secrets Manager
    subnets          = [aws_subnet.public_a.id, aws_subnet.public_b.id]
    security_groups  = [aws_security_group.postgres_sg.id]
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.postgres.arn
  }

  depends_on = [
    aws_efs_mount_target.postgres_mount_a,
    aws_efs_mount_target.postgres_mount_b
  ]
}

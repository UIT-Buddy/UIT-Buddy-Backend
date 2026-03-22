# ==========================================
# 1. TASK EXECUTION ROLE (For Fargate to pull images & write logs)
# ==========================================

# Create Role and allow ECS service to assume this Role
resource "aws_iam_role" "ecs_execution_role" {
  name = "${var.project_name}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# Attach the default AWS managed policy to the above Role
resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Grant Fargate permission to read Secrets Manager (to fetch DB password at startup)
resource "aws_iam_policy" "ecs_secrets_policy" {
  name = "${var.project_name}-ecs-secrets-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        # Allow access only to the UITBuddy secret vault
        Resource = aws_secretsmanager_secret.backend_secrets.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_secrets_attachment" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = aws_iam_policy.ecs_secrets_policy.arn
}


# ==========================================
# 2. TASK ROLE (For the Spring Boot application to call AWS services)
# ==========================================

resource "aws_iam_role" "ecs_task_role" {
  name = "${var.project_name}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# (Example) If Spring Boot later needs to use AWS S3 to store files,
# create an aws_iam_policy for S3 and attach it to this ecs_task_role.

# ==========================================
# 3. ECS EXEC POLICY (Allows using "docker exec" on Fargate via SSM)
# ==========================================

resource "aws_iam_policy" "ecs_exec_policy" {
  name = "${var.project_name}-ecs-exec-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssmmessages:CreateControlChannel",
          "ssmmessages:CreateDataChannel",
          "ssmmessages:OpenControlChannel",
          "ssmmessages:OpenDataChannel"
        ]
        Resource = "*"
      }
    ]
  })
}

# Attach to Task Role (permissions for code inside the container, not the Fargate engine)
resource "aws_iam_role_policy_attachment" "ecs_task_exec_attachment" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ecs_exec_policy.arn
}
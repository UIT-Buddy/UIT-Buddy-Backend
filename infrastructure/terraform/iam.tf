# ==========================================
# 1. TASK EXECUTION ROLE (Cho Fargate kéo Image & ghi Log)
# ==========================================

# Tạo Role và cho phép dịch vụ ECS được phép "mặc" chiếc áo Role này
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

# Gắn chính sách (Policy) mặc định của AWS vào Role trên
resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Thêm quyền cho phép Fargate đọc Secrets Manager (Để lấy mật khẩu DB lúc khởi động)
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
        # Chỉ cho phép đọc đúng két sắt UITBuddy
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
# 2. TASK ROLE (Cho ứng dụng Spring Boot gọi dịch vụ AWS)
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

# (Ví dụ) Nếu sau này Spring Boot cần dùng AWS S3 để lưu file,
# bạn sẽ tạo một aws_iam_policy cho S3 và attach vào ecs_task_role này.

# ==========================================
# 3. ECS EXEC POLICY (Cho phép dùng "docker exec" trên Fargate qua SSM)
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

# Gắn vào Task Role (quyền cho code bên trong container, không phải Fargate engine)
resource "aws_iam_role_policy_attachment" "ecs_task_exec_attachment" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ecs_exec_policy.arn
}
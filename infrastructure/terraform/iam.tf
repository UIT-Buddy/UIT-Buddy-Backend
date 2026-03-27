# ==========================================
# 1. IAM ROLE FOR EC2 (SSM + Secrets Manager)
# ==========================================

resource "aws_iam_role" "ec2_role" {
  name = "${var.project_name}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# Attach AmazonSSMManagedInstanceCore — allows SSM Session Manager (replaces SSH)
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Custom policy to read Secrets Manager
resource "aws_iam_policy" "ec2_secrets_policy" {
  name = "${var.project_name}-ec2-secrets-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = aws_secretsmanager_secret.backend_secrets.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ec2_secrets" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.ec2_secrets_policy.arn
}

# Custom policy to write to S3 (PutObject, DeleteObject)
resource "aws_iam_policy" "ec2_s3_policy" {
  name = "${var.project_name}-ec2-s3-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:GetObject"
        ]
        Resource = "${aws_s3_bucket.main.arn}/*"
      },
      {
        Effect   = "Allow"
        Action   = "s3:ListBucket"
        Resource = aws_s3_bucket.main.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ec2_s3" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.ec2_s3_policy.arn
}

# ==========================================
# 2. INSTANCE PROFILE (Attach role to EC2)
# ==========================================

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.project_name}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# ==========================================
# 3. IAM USER — S3 Client (PutObject / DeleteObject only)
# ==========================================

resource "aws_iam_user" "s3_client" {
  name = "${var.project_name}-s3-client"

  tags = { Name = "${var.project_name}-s3-client" }
}

resource "aws_iam_user_policy" "s3_client_policy" {
  name = "${var.project_name}-s3-client-write-policy"
  user = aws_iam_user.s3_client.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowS3WriteOnly"
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = "${aws_s3_bucket.main.arn}/*"
      }
    ]
  })
}

resource "aws_iam_access_key" "s3_client_key" {
  user = aws_iam_user.s3_client.name
}
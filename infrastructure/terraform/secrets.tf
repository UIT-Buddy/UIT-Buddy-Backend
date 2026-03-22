# ──────────────────────────────────────────────
# AWS Secrets Manager — Backend Secrets (DB password + Firebase)
# ──────────────────────────────────────────────
resource "aws_secretsmanager_secret" "backend_secrets" {
  name                    = "${var.project_name}/backend/secrets-v2"
  description             = "Database password and Firebase config for ${var.project_name}"
  recovery_window_in_days = 0
  tags                    = { Name = "${var.project_name}-backend-secrets" }
}

resource "aws_secretsmanager_secret_version" "backend_secrets_value" {
  secret_id = aws_secretsmanager_secret.backend_secrets.id

  # Lưu dưới dạng chuỗi JSON: db_password + firebase_json
  secret_string = jsonencode({
    POSTGRES_PASSWORD = var.db_password
    FIREBASE_JSON     = var.firebase_json
  })
}

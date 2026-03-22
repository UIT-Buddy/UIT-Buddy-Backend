# ──────────────────────────────────────────────
# EFS File System — Persistent storage for PostgreSQL
# ──────────────────────────────────────────────
resource "aws_efs_file_system" "postgres_data" {
  creation_token = "${var.project_name}-postgres-efs"
  encrypted      = true
  tags           = { Name = "${var.project_name}-postgres-data" }
}

# ──────────────────────────────────────────────
# EFS Mount Targets — one per private subnet
# ──────────────────────────────────────────────
resource "aws_efs_mount_target" "postgres_mount_a" {
  file_system_id  = aws_efs_file_system.postgres_data.id
  subnet_id       = aws_subnet.private_a.id
  security_groups = [aws_security_group.efs_sg.id]
}

resource "aws_efs_mount_target" "postgres_mount_b" {
  file_system_id  = aws_efs_file_system.postgres_data.id
  subnet_id       = aws_subnet.private_b.id
  security_groups = [aws_security_group.efs_sg.id]
}

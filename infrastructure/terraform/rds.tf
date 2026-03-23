# ──────────────────────────────────────────────
# RDS DB Subnet Group (requires 2 private subnets in different AZs)
# ──────────────────────────────────────────────
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]
  tags       = { Name = "${var.project_name}-db-subnet-group" }
}

# ──────────────────────────────────────────────
# RDS PostgreSQL Instance
# ──────────────────────────────────────────────
resource "aws_db_instance" "postgres" {
  identifier              = "${var.project_name}-postgres"
  engine                  = "postgres"
  engine_version          = "17"
  instance_class          = var.rds_instance_class
  allocated_storage       = 20
  storage_type            = "gp3"
  db_name                 = var.postgres_db
  username                = var.postgres_user
  password                = var.db_password
  db_subnet_group_name    = aws_db_subnet_group.main.name
  vpc_security_group_ids  = [aws_security_group.rds_sg.id]
  skip_final_snapshot     = true
  publicly_accessible     = false
  multi_az                = false
  backup_retention_period = 0

  tags = { Name = "${var.project_name}-postgres" }
}

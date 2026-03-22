# ──────────────────────────────────────────────
# ALB Security Group
#   Inbound : 80, 443 from Internet
#   Outbound: all
# ──────────────────────────────────────────────
resource "aws_security_group" "alb_sg" {
  name        = "${var.project_name}-alb-sg"
  description = "Allow HTTP/HTTPS from Internet to ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "HTTP from Internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS from Internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-alb-sg" }
}

# ──────────────────────────────────────────────
# Backend Security Group
#   Inbound : 8080 from ALB SG only
#   Outbound: all (needs 443 to pull images from GHCR)
# ──────────────────────────────────────────────
resource "aws_security_group" "backend_sg" {
  name        = "${var.project_name}-backend-sg"
  description = "Allow traffic from ALB to Backend"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Backend port from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-backend-sg" }
}

# ──────────────────────────────────────────────
# PostgreSQL Security Group
#   Inbound : 5432 from Backend SG only
#   Outbound: all within VPC
# ──────────────────────────────────────────────
resource "aws_security_group" "postgres_sg" {
  name        = "${var.project_name}-postgres-sg"
  description = "Allow traffic from Backend to PostgreSQL"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "PostgreSQL from Backend"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.backend_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  tags = { Name = "${var.project_name}-postgres-sg" }
}

# ──────────────────────────────────────────────
# EFS Security Group
#   Inbound : 2049 (NFS) from Postgres SG only
#   Outbound: all within VPC
# ──────────────────────────────────────────────
resource "aws_security_group" "efs_sg" {
  name        = "${var.project_name}-efs-sg"
  description = "Allow NFS from Postgres to EFS"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "NFS from Postgres"
    from_port       = 2049
    to_port         = 2049
    protocol        = "tcp"
    security_groups = [aws_security_group.postgres_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  tags = { Name = "${var.project_name}-efs-sg" }
}

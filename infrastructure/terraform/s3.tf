# ──────────────────────────────────────────────
# Data Source — Current AWS Account ID
# ──────────────────────────────────────────────
data "aws_caller_identity" "current" {}

# ──────────────────────────────────────────────
# S3 Bucket — Application Storage
# ──────────────────────────────────────────────
resource "aws_s3_bucket" "main" {
  bucket = "${var.project_name}-storage-${data.aws_caller_identity.current.account_id}"

  tags = { Name = "${var.project_name}-s3" }
}

# ──────────────────────────────────────────────
# Block ALL Public Access
# ──────────────────────────────────────────────
resource "aws_s3_bucket_public_access_block" "main" {
  bucket = aws_s3_bucket.main.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ──────────────────────────────────────────────
# CloudFront Origin Access Control (OAC)
# ──────────────────────────────────────────────
resource "aws_cloudfront_origin_access_control" "oac" {
  name                              = "${var.project_name}-oac"
  description                       = "OAC for ${var.project_name} S3 bucket"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# ──────────────────────────────────────────────
# CloudFront Distribution
# ──────────────────────────────────────────────
resource "aws_cloudfront_distribution" "cdn" {
  enabled             = true
  default_root_object = "index.html"
  comment             = "${var.project_name} CDN"
  price_class         = "PriceClass_200"

  origin {
    domain_name              = aws_s3_bucket.main.bucket_regional_domain_name
    origin_id                = "s3-${aws_s3_bucket.main.id}"
    origin_access_control_id = aws_cloudfront_origin_access_control.oac.id
  }

  default_cache_behavior {
    target_origin_id       = "s3-${aws_s3_bucket.main.id}"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true

    # AWS Managed CachingOptimized policy
    cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = { Name = "${var.project_name}-cdn" }
}

# ──────────────────────────────────────────────
# S3 Bucket Policy — Allow GetObject ONLY from CloudFront
# ──────────────────────────────────────────────
resource "aws_s3_bucket_policy" "main" {
  bucket = aws_s3_bucket.main.id

  depends_on = [aws_s3_bucket_public_access_block.main]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontServicePrincipalReadOnly"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.main.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.cdn.arn
          }
        }
      }
    ]
  })
}

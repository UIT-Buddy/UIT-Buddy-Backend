output "alb_dns_name" {
  description = "DNS name của Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "backend_url" {
  description = "URL để truy cập backend qua ALB"
  value       = "http://${aws_lb.main.dns_name}"
}

output "health_check_url" {
  description = "URL kiểm tra trạng thái backend"
  value       = "http://${aws_lb.main.dns_name}/actuator/health"
}

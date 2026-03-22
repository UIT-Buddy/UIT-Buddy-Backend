output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "backend_url" {
  description = "URL to access the backend via ALB"
  value       = "http://${aws_lb.main.dns_name}"
}

output "health_check_url" {
  description = "URL to check backend health status"
  value       = "http://${aws_lb.main.dns_name}/actuator/health"
}

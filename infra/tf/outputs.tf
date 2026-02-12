// infra-tf-backend/outputs.tf

output "be_instance_id" {
  description = "Backend EC2 instance ID"
  value       = aws_instance.be.id
}

output "be_public_ip" {
  description = "Backend EC2 public IP"
  value       = aws_instance.be.public_ip
}

output "be_private_ip" {
  description = "Backend EC2 private IP"
  value       = aws_instance.be.private_ip
}

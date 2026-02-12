variable "aws_region" {
  description = "AWS region to deploy backend EC2 for Tomcat"
  type        = string
  default     = "us-east-1"
}


variable "instance_type" {
  description = "EC2 instance type for Tomcat server"
  type        = string
  default     = "t3.micro"
}


# pass key_name and my_ip_cidr via -var or a terraform.tfvars file.

variable "key_name" {
  description = "Existing key pair name for SSH access to EC2 instance"
  type        = string
}

variable "my_ip_cidr" {
  description = "Your /32 IP address in CIDR notation for SSH access"
  type        = string
}



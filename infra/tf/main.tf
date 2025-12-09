
# Find latest Amazon Linux 2023 AMI

# data = read existing stuff from AWS
# resource = create new stuff in AWS


data "aws_ami" "al2023" {
  most_recent = true

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }


  filter {
    name   = "architecture"
    values = ["x86_64"]
  }


  owners = ["137112412989"] # Amazon
}


# Security group for backend
resource "aws_security_group" "be_sg" {
  name        = "hellowar-be-sg"
  description = "Security group for hellowar tomcat backend EC2"
  vpc_id      = data.aws_vpc.default.id

  # SSH from your IP (debug purposes only)
  ingress {
    from_port   = 22 # start of port range
    to_port     = 22 # end of port range
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] #[var.my_ip_cidr]
  }


  # Tomcat 8080 - from my IP ; later only from fe SG
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] #[var.my_ip_cidr]
  }


  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "hellowar-be-sg" #Tags = metadata in AWS. Name is what the console shows by default.
  }

}



# Using default VPC
data "aws_vpc" "default" {
  default = true
}


data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# User data script: download & run bootstrap-tomcat.sh
locals {
  bootstrap_url = "https://raw.githubusercontent.com/devopsbyte-internal/proj-hellowar-java-service/refs/heads/main/infra/tomcat/bootstrap-tomcat.sh"
}


resource "aws_instance" "be" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = data.aws_subnets.default.ids[0]
  vpc_security_group_ids = [aws_security_group.be_sg.id]


  # remove data "template_file" block and uncomment below to use templatefile() function directly
  user_data = templatefile("${path.module}/userdata-tomcat.sh.tpl", {
    bootstrap_url = local.bootstrap_url
  })

  tags = {
    Name = "hellowar-tomcat-be"
    Role = "backend"
  }
}
    
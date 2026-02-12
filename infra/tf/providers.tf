terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~>5.0"
    }
  }

  backend "local" {}

}


provider "aws" {
  region = var.aws_region
}

# Authentication:
# AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN
# AWS_PROFILE pointing to an entry in ~/.aws/credentials

# aws configure --profile tf-lab
# export AWS_PROFILE=tf-lab

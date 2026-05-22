variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "hermes-finance"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "owner" {
  type    = string
  default = "felipe"
}

variable "cost_center" {
  type    = string
  default = "learning"
}

variable "vpc_cidr" {
  type    = string
  default = "10.20.0.0/16"
}

variable "public_subnet_a_cidr" {
  type    = string
  default = "10.20.1.0/24"
}

variable "public_subnet_b_cidr" {
  type    = string
  default = "10.20.2.0/24"
}

variable "private_subnet_a_cidr" {
  type    = string
  default = "10.20.11.0/24"
}

variable "private_subnet_b_cidr" {
  type    = string
  default = "10.20.12.0/24"
}

variable "allowed_app_cidrs" {
  type    = list(string)
  default = ["0.0.0.0/0"]
}

variable "allowed_ssh_cidrs" {
  type = list(string)
}

variable "app_port" {
  type    = number
  default = 8080
}

variable "ec2_instance_type" {
  type    = string
  default = "t3.micro"
}

variable "rds_instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_name" {
  type    = string
  default = "hermes"
}

variable "db_username" {
  type = string
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "ssm_parameter_prefix" {
  type    = string
  default = "/hermes/dev/"
}

variable "plain_ssm_parameters" {
  type        = map(string)
  description = "Non-sensitive SSM parameters. Keys must include full path."
}

variable "secure_ssm_parameters" {
  type        = map(string)
  description = "Sensitive SSM parameters. Keys must include full path."
}

variable "github_owner" {
  type = string
}

variable "github_repo" {
  type = string
}

variable "existing_oidc_provider_arn" {
  type = string
}

variable "aws_region" {
  description = "AWS region used for bootstrap resources."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name prefix for resources."
  type        = string
  default     = "hermes-finance"
}

variable "environment" {
  description = "Environment name for bootstrap resources."
  type        = string
  default     = "global"
}

variable "owner" {
  description = "Owner tag value."
  type        = string
  default     = "felipe"
}

variable "cost_center" {
  description = "Cost center tag value."
  type        = string
  default     = "learning"
}
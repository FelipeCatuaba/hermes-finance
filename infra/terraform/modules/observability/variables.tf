variable "name_prefix" {
  type = string
}

variable "monthly_budget_usd" {
  type    = number
  default = 5
}

variable "alert_email" {
  type = string
}

variable "ec2_instance_id" {
  type = string
}

variable "rds_instance_identifier" {
  type = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
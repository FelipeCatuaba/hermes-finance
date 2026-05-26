variable "name_prefix" {
  type = string
}

variable "existing_oidc_provider_arn" {
  type = string

  validation {
    condition     = length(trimspace(var.existing_oidc_provider_arn)) > 0
    error_message = "existing_oidc_provider_arn must be provided."
  }
}

variable "github_owner" {
  type = string
}

variable "github_repo" {
  type = string
}

variable "ecr_repository_arn" {
  type = string
}

variable "tags" {
  type    = map(string)
  default = {}
}

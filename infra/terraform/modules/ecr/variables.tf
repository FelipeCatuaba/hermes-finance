variable "repository_name" {
  type = string
}

variable "image_retention_count" {
  type    = number
  default = 20
}

variable "tags" {
  type    = map(string)
  default = {}
}
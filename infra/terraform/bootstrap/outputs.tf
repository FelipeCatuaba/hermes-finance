output "terraform_state_bucket_name" {
  description = "S3 bucket name for Terraform remote state."
  value       = aws_s3_bucket.terraform_state.bucket
}

output "terraform_lock_table_name" {
  description = "DynamoDB table name for Terraform state locking."
  value       = aws_dynamodb_table.terraform_locks.name
}

output "aws_account_id" {
  description = "Current AWS account ID."
  value       = data.aws_caller_identity.current.account_id
}
output "gha_terraform_role_arn" {
  value = aws_iam_role.gha_terraform.arn
}

output "gha_deploy_role_arn" {
  value = aws_iam_role.gha_deploy.arn
}
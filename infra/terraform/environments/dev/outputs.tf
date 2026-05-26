output "ec2_public_ip" {
  value = module.ec2.public_ip
}

output "ec2_instance_id" {
  value = module.ec2.instance_id
}

output "rds_endpoint" {
  value = module.rds.db_endpoint
}

output "rds_instance_identifier" {
  value = module.rds.db_instance_identifier
}

output "ecr_repository_url" {
  value = module.ecr.repository_url
}

output "gha_terraform_role_arn" {
  value = module.iam_oidc.gha_terraform_role_arn
}

output "gha_deploy_role_arn" {
  value = module.iam_oidc.gha_deploy_role_arn
}

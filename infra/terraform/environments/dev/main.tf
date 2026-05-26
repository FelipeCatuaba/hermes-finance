locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
    Owner       = var.owner
    CostCenter  = var.cost_center
  }
}

module "network" {
  source = "../../modules/network"

  name = local.name_prefix

  vpc_cidr = var.vpc_cidr

  public_subnets = {
    a = {
      cidr = var.public_subnet_a_cidr
      az   = "${var.aws_region}a"
    }
    b = {
      cidr = var.public_subnet_b_cidr
      az   = "${var.aws_region}b"
    }
  }

  private_subnets = {
    a = {
      cidr = var.private_subnet_a_cidr
      az   = "${var.aws_region}a"
    }
    b = {
      cidr = var.private_subnet_b_cidr
      az   = "${var.aws_region}b"
    }
  }

  tags = local.common_tags
}

module "security" {
  source = "../../modules/security"

  name = local.name_prefix

  vpc_id            = module.network.vpc_id
  app_port          = var.app_port
  allowed_app_cidrs = var.allowed_app_cidrs
  allowed_ssh_cidrs = var.allowed_ssh_cidrs

  tags = local.common_tags
}

module "ecr" {
  source = "../../modules/ecr"

  repository_name = "${local.name_prefix}-api"

  tags = local.common_tags
}

module "rds" {
  source = "../../modules/rds"

  name                  = local.name_prefix
  private_subnet_ids    = module.network.private_subnet_ids
  rds_security_group_id = module.security.rds_security_group_id
  db_name               = var.db_name
  db_username           = var.db_username
  db_password           = var.db_password
  instance_class        = var.rds_instance_class

  tags = local.common_tags
}

module "ec2" {
  source = "../../modules/ec2"

  name = local.name_prefix

  aws_region            = var.aws_region
  instance_type         = var.ec2_instance_type
  public_subnet_id      = module.network.public_subnet_ids[0]
  ec2_security_group_id = module.security.ec2_security_group_id
  ecr_repository_url    = module.ecr.repository_url
  ecr_repository_arn    = module.ecr.repository_arn
  ssm_parameter_prefix  = var.ssm_parameter_prefix
  app_port              = var.app_port

  tags = local.common_tags
}

module "iam_oidc" {
  source = "../../modules/iam_oidc"

  name_prefix               = local.name_prefix
  existing_oidc_provider_arn = var.existing_oidc_provider_arn
  github_owner              = var.github_owner
  github_repo               = var.github_repo
  ecr_repository_arn        = module.ecr.repository_arn

  tags = local.common_tags
}

resource "aws_ssm_parameter" "plain" {
  for_each = merge(
    {
      "${var.ssm_parameter_prefix}SPRING_DATASOURCE_HOST" = module.rds.db_endpoint
      "${var.ssm_parameter_prefix}SPRING_DATASOURCE_DB"   = var.db_name
      "${var.ssm_parameter_prefix}SPRING_DATASOURCE_USERNAME" = var.db_username
    },
    var.plain_ssm_parameters
  )

  name      = each.key
  type      = "String"
  value     = each.value
  overwrite = true

  tags = local.common_tags
}

resource "aws_ssm_parameter" "secure" {
  for_each = merge(
    {
      "${var.ssm_parameter_prefix}SPRING_DATASOURCE_PASSWORD" = var.db_password
    },
    var.secure_ssm_parameters
  )

  name      = each.key
  type      = "SecureString"
  value     = each.value
  overwrite = true

  tags = local.common_tags
}

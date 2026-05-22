locals {
  repo_sub_pattern = "repo:${var.github_owner}/${var.github_repo}:*"
}

resource "aws_iam_role" "gha_terraform" {
  name = "${var.name_prefix}-gha-terraform"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = var.existing_oidc_provider_arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = local.repo_sub_pattern
          }
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy" "gha_terraform" {
  name = "${var.name_prefix}-gha-terraform-policy"
  role = aws_iam_role.gha_terraform.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ec2:*",
          "rds:*",
          "iam:*",
          "ecr:*",
          "ssm:*",
          "sns:*",
          "cloudwatch:*",
          "budgets:*",
          "dynamodb:*",
          "s3:*"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role" "gha_deploy" {
  name = "${var.name_prefix}-gha-deploy"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = var.existing_oidc_provider_arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = local.repo_sub_pattern
          }
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy" "gha_deploy" {
  name = "${var.name_prefix}-gha-deploy-policy"
  role = aws_iam_role.gha_deploy.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:CompleteLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:InitiateLayerUpload",
          "ecr:PutImage",
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer"
        ]
        Resource = var.ecr_repository_arn
      },
      {
        Effect = "Allow"
        Action = [
          "ssm:SendCommand",
          "ssm:GetCommandInvocation"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ssm:ListCommandInvocations"
        ]
        Resource = "*"
      }
    ]
  })
}

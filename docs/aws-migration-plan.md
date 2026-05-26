# AWS Migration Plan - Hermes Finance

## Goals

- Migrate Hermes backend runtime to AWS using Docker and Terraform.
- Use GitHub Actions with OIDC (no static AWS keys in CI).
- Keep monthly spend capped to USD 5 for a personal environment.
- Preserve current backend behavior while preparing auth provider switch (Clerk -> Cognito).

## Current Decisions

- Region: `us-east-1`
- Runtime v1: `EC2 t3.micro` (public subnet), no ALB, no NAT gateway
- Database v1: `RDS PostgreSQL db.t3.micro` (private subnet)
- DNS v1: external free DNS provider (no Route 53 in this phase)
- Terraform state: S3 + DynamoDB lock table
- Auth v1: Clerk remains active
- Auth v2: Cognito adapter planned without domain coupling

## Terraform Scope

### Bootstrap Stack

Path: `infra/terraform/bootstrap`

Creates:
- S3 bucket for Terraform remote state
- DynamoDB table for state locking

### Environment Stack (dev)

Path: `infra/terraform/environments/dev`

Uses modules:
- `network`: VPC, subnets, route tables, internet gateway
- `security`: security groups for EC2 and RDS
- `ecr`: ECR repository + lifecycle policy
- `rds`: private PostgreSQL instance + subnet group
- `ec2`: app host instance + IAM instance profile + deploy bootstrap script
- `iam_oidc`: GitHub OIDC provider + `gha-terraform` and `gha-deploy` roles
- `observability`: (desativado no Terraform atual; budget gerenciado manualmente no console AWS)

Also writes application parameters to SSM Parameter Store (`/hermes/dev/*`).

## GitHub Actions Scope

- `ci.yml`: Maven tests for PR and main
- `terraform-plan.yml`: format/validate/plan on infra PRs
- `terraform-apply.yml`: apply on main with environment approval
- `deploy-ec2.yml`: build and push Docker image to ECR, deploy to EC2 through SSM

### Required GitHub Secrets

- `AWS_TERRAFORM_ROLE_ARN`
- `AWS_DEPLOY_ROLE_ARN`

### Required GitHub Repository Variables

- `ECR_REPOSITORY`
- `EC2_INSTANCE_ID`

## Cost Guardrails

- No ALB, no NAT gateway in v1
- Single EC2 instance and single RDS instance
- AWS Budget: USD 5 with notifications at 50/80/100%
- Tagging strategy in all resources (`Project`, `Environment`, `Owner`, `CostCenter`)

## Rollout Roadmap (5 days)

1. Day 1: Bootstrap Terraform backend + OIDC IAM + repo configuration
2. Day 2: Provision VPC/EC2/RDS/ECR/SSM and validate infra health
3. Day 3: Configure CI/CD pipelines and first deploy through GitHub Actions
4. Day 4: Add hardening and runbooks (rollback, alarms, backup checks)
5. Day 5: Start auth abstraction validation and Cognito migration design

## Runbook - Deployment

1. Push code to `main`
2. `deploy-ec2.yml` builds image and pushes to ECR
3. Workflow runs SSM command on EC2: `sudo /opt/hermes/deploy.sh <image-tag>`
4. EC2 script reads SSM params and restarts the container

## Runbook - Rollback

1. Select last known good image tag from ECR
2. Execute on EC2:

```bash
sudo /opt/hermes/deploy.sh <previous-tag>
```

3. Validate `/api/health`

## Learning Checklist

### AWS

- VPC basics, subnet routing, SG boundaries
- IAM role trust and least privilege
- RDS private networking and access patterns
- Budgets and CloudWatch alerting

### Docker

- Build, tag and publish to ECR
- Runtime env injection and health validation
- Rollback by immutable tag

### Terraform

- Module composition and environment layering
- Remote state and locking patterns
- Idempotency and drift detection workflow

### GitHub Actions

- OIDC federation to AWS
- PR plan and protected apply
- Progressive deployment automation

## Next Phase (Cognito)

- Add provider adapter with `AUTH_PROVIDER=clerk|cognito`
- Keep `AuthIdentityProvider` contract stable
- Introduce Cognito JWT validator and mapping layer
- Switch by configuration only after integration tests pass

# Terraform Bootstrap

This stack creates the remote backend resources for Terraform state:

- S3 bucket with versioning and SSE for state files
- DynamoDB table for state locking

## Usage

```bash
terraform init
terraform plan
terraform apply
```

Then configure the `infra/terraform/environments/dev/backend.hcl` file with the generated bucket/table values and run:

```bash
terraform init -reconfigure -backend-config=backend.hcl
```
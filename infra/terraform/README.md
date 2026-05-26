# Terraform Infrastructure

## Layout

- `bootstrap/`: creates S3 backend bucket and DynamoDB lock table
- `environments/dev/`: dev environment stack
- `modules/`: reusable AWS modules

## Bootstrap

```bash
cd infra/terraform/bootstrap
terraform init
terraform apply
```

Copy output values and configure `environments/dev/backend.hcl`.

## Environment Dev

```bash
cd infra/terraform/environments/dev
cp backend.hcl.example backend.hcl
cp terraform.tfvars.example terraform.tfvars
terraform init -reconfigure -backend-config=backend.hcl
terraform plan
terraform apply
```

## Notes

- This stack is optimized for low-cost learning environments.
- The first phase intentionally avoids ALB and NAT Gateway.
# Environment dev

## First run

1. Bootstrap state backend in `../../bootstrap`.
2. Copy `backend.hcl.example` to `backend.hcl` and update values.
3. Initialize remote backend:

```bash
terraform init -reconfigure -backend-config=backend.hcl
```

4. Copy `terraform.tfvars.example` to `terraform.tfvars` and fill your values.
5. Run:

```bash
terraform plan
terraform apply
```
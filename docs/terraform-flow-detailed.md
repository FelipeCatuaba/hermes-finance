# Fluxo Terraform no Hermes Finance (Detalhado)

Este documento explica, passo a passo, como o fluxo de infraestrutura foi montado no projeto e como operar com seguranca.

## 1) Visao geral

O fluxo foi desenhado para:

- versionar infraestrutura como codigo (Terraform)
- evitar credenciais AWS estaticas no GitHub (OIDC)
- manter custo baixo (budget de US$5/m)
- permitir deploy continuo da API Docker na EC2

Branch de deploy do ambiente dev usada pelos workflows: `develop`.

## 2) Estrutura criada

### 2.1 Pasta de bootstrap

`infra/terraform/bootstrap`

Responsabilidade:

- criar bucket S3 para guardar o state remoto do Terraform
- criar tabela DynamoDB para lock de state

Por que existe?

- o Terraform precisa de um "lugar seguro" para salvar estado compartilhado
- o lock evita duas execucoes simultaneas corrompendo o mesmo state

### 2.2 Ambiente dev

`infra/terraform/environments/dev`

Responsabilidade:

- orquestrar todos os modulos
- definir variaveis de ambiente (rede, DB, GitHub, budget, etc.)
- publicar outputs com dados importantes (IP EC2, endpoint RDS, ARNs)

### 2.3 Modulos

`infra/terraform/modules/*`

- `network`: VPC, subnets publicas/privadas, route tables, internet gateway
- `security`: security groups da EC2 e do RDS
- `ecr`: repositorio de imagem Docker
- `rds`: instancia PostgreSQL privada
- `ec2`: instancia da aplicacao + profile IAM + bootstrap de deploy
- `iam_oidc`: OIDC provider e roles assumidas pelos workflows
- `observability`: removido do ambiente dev atual (budget no console AWS)

## 3) Fluxo de execucao do Terraform

## 3.1 Fase 1 - Bootstrap (uma vez)

Executado localmente:

```bash
cd infra/terraform/bootstrap
terraform init
terraform apply
```

Resultado:

- bucket S3 criado
- tabela DynamoDB criada

Depois disso, voce copia os nomes para o `backend.hcl` do ambiente dev.

## 3.2 Fase 2 - Migrar ambiente dev para backend remoto

No `infra/terraform/environments/dev`:

1. copiar `backend.hcl.example` para `backend.hcl`
2. preencher bucket/tabela/regiao
3. inicializar backend remoto:

```bash
terraform init -reconfigure -backend-config=backend.hcl
```

A partir daqui, state do `dev` fica no S3 com lock via DynamoDB.

## 3.3 Fase 3 - Planejar e aplicar

Local:

```bash
terraform plan
terraform apply
```

Ou via pipeline (recomendado para rotina):

- PR -> `terraform-plan.yml`
- Merge em `develop` -> `terraform-apply.yml`

## 4) Fluxo GitHub Actions

## 4.1 `ci.yml`

Dispara em PR e push para `develop` (e tambem em `master` para CI geral).

- checkout
- setup Java 21
- `./mvnw test`

Objetivo: garantir que mudancas nao quebram aplicacao.

## 4.2 `terraform-plan.yml`

Dispara em PR com mudancas em `infra/terraform/**`.

- autentica na AWS via OIDC (`AWS_TERRAFORM_ROLE_ARN`)
- gera `backend.hcl` dinamico usando vars do repo
- gera `terraform.auto.tfvars` dinamico usando vars/secrets
- `terraform init`
- `terraform fmt -check`
- `terraform validate`
- `terraform plan`
- publica artefato com plano

Objetivo: revisao segura antes do merge.

## 4.3 `terraform-apply.yml`

Dispara em push na `develop` (infra dev) e manual (`workflow_dispatch`).

- mesmo setup do plan
- roda plan e apply
- usa `environment` para exigir aprovacao humana (se configurado no GitHub)

Objetivo: aplicar infraestrutura de forma controlada.

## 4.4 `deploy-ec2.yml`

Dispara em push para `develop` (ignorando docs e infra) e manual.

- autentica via OIDC (`AWS_DEPLOY_ROLE_ARN`)
- login ECR
- build da imagem e push (tag sha + latest)
- envia comando SSM para EC2 executar deploy

Objetivo: publicar nova versao da API na EC2.

## 5) Como o deploy funciona na EC2

No provisionamento da EC2, o user-data cria `/opt/hermes/deploy.sh`.

Esse script:

1. le parametros no SSM (`/hermes/dev/*`)
2. faz login no ECR
3. puxa imagem por tag
4. remove container antigo
5. sobe container novo com env vars

Assim, o segredo fica na AWS (SSM), nao no YAML do workflow.

## 6) Secrets e variables do GitHub

## 6.1 Secrets (sensivel)

- `AWS_TERRAFORM_ROLE_ARN`
- `AWS_DEPLOY_ROLE_ARN`
- `TF_DB_PASSWORD`
- `TF_CLERK_WEBHOOK_SECRET`

## 6.2 Variables (nao sensivel)

- `TF_STATE_BUCKET`
- `TF_STATE_LOCK_TABLE`
- `TF_ALLOWED_SSH_CIDR`
- `TF_DB_USERNAME`
- `TF_GITHUB_OWNER`
- `TF_GITHUB_REPO`
- `TF_CLERK_ISSUER_URI`
- `TF_CLERK_JWKS_URI`
- `TF_ALLOWED_ORIGINS`
- `ECR_REPOSITORY`
- `EC2_INSTANCE_ID`

## 7) Custo e protecoes

Medidas aplicadas no design:

- sem NAT Gateway no v1
- sem ALB no v1
- ambiente unico (dev)
- EC2 `t3.micro`
- RDS `db.t3.micro`
- budget de US$5 com alertas em 50/80/100%

Sugestao operacional:

- revisar Budget e Cost Explorer semanalmente
- desligar recursos quando nao estiver usando por longos periodos

## 8) Rotina recomendada de trabalho

1. Criar branch de feature
2. Abrir PR
3. Ver `ci` + `terraform plan`
4. Aprovar e mergear em `develop`
5. Aprovar `terraform-apply` (se ambiente exigir)
6. Validar deploy da app
7. Checar `/api/health`

## 9) Troubleshooting rapido

### `terraform init` falha no backend

- conferir bucket/table/regiao no `backend.hcl` ou vars do workflow
- conferir permissoes da role Terraform

### `terraform plan` pede variavel faltante

- conferir vars/secrets no GitHub
- conferir `terraform.auto.tfvars` gerado pelo workflow

### deploy nao atualiza EC2

- confirmar `EC2_INSTANCE_ID` correto
- checar SSM Managed Instance online
- inspecionar output de `get-command-invocation`

### app sobe mas nao conecta no banco

- conferir SG do RDS permitindo apenas SG da EC2
- conferir parametros SSM de DB
- conferir endpoint e credenciais

## 10) O que fica para a proxima fase

- adicionar HTTPS robusto (ALB/ACM ou reverse proxy com certificado)
- ambiente `prd` separado
- introduzir Cognito mantendo modelo agnostico por provider (`AUTH_PROVIDER`)
- evoluir politicas IAM para menor privilegio mais estrito


## Reaproveitar OIDC ja criado no console

Se voce ja criou manualmente o provider `token.actions.githubusercontent.com` na AWS, configure no `terraform.tfvars` do `dev`:

```hcl
existing_oidc_provider_arn = "arn:aws:iam::<ACCOUNT_ID>:oidc-provider/token.actions.githubusercontent.com"
```

Assim o Terraform cria apenas as roles/policies do GitHub Actions e reutiliza o provider existente, evitando erro de recurso duplicado.

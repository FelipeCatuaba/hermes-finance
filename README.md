# hermes-finance (Backend)

API Spring Boot do HERMES.

## Setup de variaveis

1. Copie `.env.example` para `.env`
2. Preencha as variáveis de autenticação se estiver usando um provedor externo (ex.: `ISSUER_URI`, `JWKS_URI`) ou deixe vazias para uso de JWT local.
3. Ajuste `ALLOWED_ORIGINS` para a URL do frontend (ex.: `http://localhost:5173`)
4. Nunca versione `.env`

Se não estiver usando um provedor externo, deixe as variáveis de issuer/JWKS em branco e configure sua estratégia JWT local.

## Banco local com Docker

```bash
docker compose up -d
```

## Rodar backend

```bash
mvn spring-boot:run
```

## Autenticação

O backend valida JWT (iss, exp/nbf) e pode ser configurado para usar um provedor externo através das variáveis de ambiente `ISSUER_URI` e `JWKS_URI`. Se utilizar webhooks de provedores externos, configure o segredo correspondente no `.env` e no dashboard do provedor.

Em produção, webhooks sem assinatura/segredo válido devem ser rejeitados.

## Hardening de Seguranca

- JWT validado por assinatura + `iss` + `exp/nbf` e, opcionalmente, `aud` (`JWT_ALLOWED_AUDIENCES`)
- Webhook com validacao Svix + janela de timestamp (`CLERK_WEBHOOK_MAX_TIMESTAMP_SKEW_SECONDS`)
- Protecao anti-replay de webhook por `svix-id` com TTL (`CLERK_WEBHOOK_REPLAY_CACHE_TTL_SECONDS`)
- Rate limiting por IP para `/api/public/**` e `/api/webhooks/**`
- Headers de seguranca habilitados (`HSTS`, `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`)

Variaveis relevantes em `.env.example`:

- `JWT_ALLOWED_AUDIENCES`
- `CLERK_WEBHOOK_MAX_TIMESTAMP_SKEW_SECONDS`
- `CLERK_WEBHOOK_REPLAY_CACHE_TTL_SECONDS`
- `RATE_LIMIT_ENABLED`
- `RATE_LIMIT_MAX_REQUESTS`
- `RATE_LIMIT_WINDOW_SECONDS`

## AWS + Terraform + GitHub Actions

O plano detalhado de migracao esta em `docs/aws-migration-plan.md`.
Explicacao detalhada do fluxo Terraform/CI esta em `docs/terraform-flow-detailed.md`.

Estrutura de IaC:

- `infra/terraform/bootstrap`: cria bucket S3 de state e tabela DynamoDB de lock.
- `infra/terraform/environments/dev`: ambiente AWS dev.
- `infra/terraform/modules/*`: modulos reutilizaveis.

Workflows criados:

- `.github/workflows/ci.yml`
- `.github/workflows/terraform-plan.yml`
- `.github/workflows/terraform-apply.yml`
- `.github/workflows/deploy-ec2.yml`

Para o CI/CD funcionar com OIDC:

- Defina os secrets:
  - `AWS_TERRAFORM_ROLE_ARN`
  - `AWS_DEPLOY_ROLE_ARN`
  - `TF_DB_PASSWORD`
  - `TF_CLERK_WEBHOOK_SECRET`
- Defina os variables:
  - `TF_STATE_BUCKET`
  - `TF_STATE_LOCK_TABLE`
  - `TF_ALLOWED_SSH_CIDR`
  - `TF_DB_USERNAME`
  - `TF_GITHUB_OWNER`
  - `TF_GITHUB_REPO`
  - `TF_EXISTING_OIDC_PROVIDER_ARN`
  - `TF_AUTH_PROVIDER`
  - (Removed Clerk-related Terraform variables; configure auth provider variables as needed)
  - `TF_ALLOWED_ORIGINS`
  - `TF_DB_POOL_NAME`
  - `TF_DB_MAX_POOL_SIZE`
  - `TF_DB_MIN_IDLE`
  - `TF_DB_CONNECTION_TIMEOUT_MS`
  - `TF_DB_IDLE_TIMEOUT_MS`
  - `TF_DB_MAX_LIFETIME_MS`
  - `TF_LOG_LEVEL`
  - `ECR_REPOSITORY`
  - `EC2_INSTANCE_ID`

Orcamento AWS (Budget) esta gerenciado manualmente no console e nao mais via Terraform.

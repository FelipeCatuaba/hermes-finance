# hermes-finance (Backend)

API Spring Boot do HERMES.

## Setup de variaveis

1. Copie `.env.example` para `.env`
2. Preencha `CLERK_JWKS_URI`, `CLERK_ISSUER_URI` e `CLERK_WEBHOOK_SECRET` (Clerk Dashboard)
3. Ajuste `ALLOWED_ORIGINS` para a URL do frontend (ex.: `http://localhost:5173`)
4. Nunca versione `.env`

`CLERK_SECRET_KEY` e opcional: so necessaria se o backend passar a chamar a API administrativa do Clerk.

## Banco local com Docker

```bash
docker compose up -d
```

## Rodar backend

```bash
mvn spring-boot:run
```

## Clerk - checklist rapido

| Onde | O que |
|------|--------|
| Clerk Dashboard -> Webhooks | Endpoint `POST https://<api>/api/webhooks/clerk`, eventos `user.created`, `user.updated`, `user.deleted` |
| Clerk Dashboard -> Webhooks | Copiar **Signing Secret** -> `CLERK_WEBHOOK_SECRET` |
| Clerk Dashboard -> API Keys | **Issuer** e **JWKS URL** -> `CLERK_ISSUER_URI` / `CLERK_JWKS_URI` |
| Frontend | `pk_...` (publishable key) - nao vai no backend |

Em producao, webhooks sem `CLERK_WEBHOOK_SECRET` valido sao rejeitados.

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
  - `TF_CLERK_ISSUER_URI`
  - `TF_CLERK_JWKS_URI`
  - `TF_CLERK_WEBHOOK_REQUIRE_SIGNATURE`
  - `TF_CLERK_WEBHOOK_MAX_TIMESTAMP_SKEW_SECONDS`
  - `TF_CLERK_WEBHOOK_REPLAY_CACHE_TTL_SECONDS`
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

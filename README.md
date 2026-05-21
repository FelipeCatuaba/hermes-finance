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

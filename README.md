# hermes-finance (Backend)

API Spring Boot do HERMES.

## Setup de vari?veis

1. Copie `.env.example` para `.env`
2. Preencha `CLERK_JWKS_URI`, `CLERK_ISSUER_URI` e `CLERK_WEBHOOK_SECRET` (Clerk Dashboard)
3. Ajuste `ALLOWED_ORIGINS` para a URL do frontend (ex.: `http://localhost:5173`)
4. Nunca versione `.env`

`CLERK_SECRET_KEY` ? opcional — s? necess?ria se o backend passar a chamar a API administrativa do Clerk.

## Banco local com Docker

```bash
docker compose up -d
```

## Rodar backend

```bash
mvn spring-boot:run
```

## Clerk — checklist r?pido

| Onde | O qu? |
|------|--------|
| Clerk Dashboard ? Webhooks | Endpoint `POST https://<api>/api/webhooks/clerk`, eventos `user.created`, `user.updated`, `user.deleted` |
| Clerk Dashboard ? Webhooks | Copiar **Signing Secret** ? `CLERK_WEBHOOK_SECRET` |
| Clerk Dashboard ? API Keys | **Issuer** e **JWKS URL** ? `CLERK_ISSUER_URI` / `CLERK_JWKS_URI` |
| Frontend | `pk_...` (publishable key) — n?o vai no backend |

Em **produ??o** (`spring.profiles.active=prod`), webhooks sem `CLERK_WEBHOOK_SECRET` v?lido s?o rejeitados.

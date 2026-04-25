# hermes-finance (Backend)

API Spring Boot do HERMES.

## Setup seguro de variáveis

1. Copie `.env.example` para `.env`
2. Preencha segredos reais em `.env` (`JWT_SECRET`, `ARGON2_PEPPER`)
3. Nunca versione `.env`

## Banco local com Docker

```bash
docker compose up -d
```

## Rodar backend

```bash
mvn spring-boot:run
```

## Observação

Sem `JWT_SECRET` e `ARGON2_PEPPER` definidos, a aplicação falha no startup por segurança.

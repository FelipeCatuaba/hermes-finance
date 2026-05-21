-- Migração para autenticação com Clerk
-- Remove campos de credenciais locais, adiciona clerk_id como chave de vínculo

-- Limpa dados existentes para garantir integridade referencial
-- (ambiente de desenvolvimento — sem dados de produção)
TRUNCATE TABLE refresh_tokens CASCADE;
TRUNCATE TABLE users CASCADE;

-- Remove tabela de refresh tokens (gerenciado pelo Clerk)
DROP TABLE IF EXISTS refresh_tokens;

-- Remove colunas de autenticação local da tabela users
ALTER TABLE users DROP COLUMN IF EXISTS password;
ALTER TABLE users DROP COLUMN IF EXISTS role;
ALTER TABLE users DROP COLUMN IF EXISTS active;

-- Adiciona clerk_id como identificador externo
ALTER TABLE users ADD COLUMN clerk_id VARCHAR(64);
ALTER TABLE users ALTER COLUMN clerk_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_clerk_id_unique UNIQUE (clerk_id);

CREATE INDEX idx_users_clerk_id ON users(clerk_id);

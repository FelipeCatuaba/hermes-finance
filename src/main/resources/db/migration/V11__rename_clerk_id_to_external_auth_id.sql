-- Torna o schema agnostico ao provider de autenticacao.
-- Seguranca para ambientes ja migrados: executa apenas se a coluna antiga existir.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'clerk_id'
    ) THEN
        ALTER TABLE users RENAME COLUMN clerk_id TO external_auth_id;
    END IF;
END $$;

-- Ajusta nome da constraint unica (se ainda existir no nome antigo)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_clerk_id_unique'
    ) THEN
        ALTER TABLE users RENAME CONSTRAINT users_clerk_id_unique TO users_external_auth_id_unique;
    END IF;
END $$;

-- Ajusta nome do indice (se ainda existir no nome antigo)
ALTER INDEX IF EXISTS idx_users_clerk_id RENAME TO idx_users_external_auth_id;


-- =============================================
-- V1: Criação da tabela de Tasks
-- =============================================

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índice para ordenação por data de criação
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);

-- Dados iniciais para teste
INSERT INTO tasks (title, done, created_at) VALUES
    ('Configurar ambiente de desenvolvimento', true, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('Implementar autenticação', false, CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('Criar testes unitários', false, CURRENT_TIMESTAMP);

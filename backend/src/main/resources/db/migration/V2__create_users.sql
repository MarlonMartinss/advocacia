-- =============================================
-- V2: Criação da tabela de usuários
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índice para busca por username
CREATE INDEX idx_users_username ON users(username);

-- Inserir usuário admin (senha: 1234 com BCrypt)
-- Hash BCrypt de '1234': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.h8rC8wrCXCjBQxDCK.
INSERT INTO users (username, password, name, email, role, active, created_at)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.h8rC8wrCXCjBQxDCK.',
    'Administrador',
    'admin@advocacia.com',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP
);

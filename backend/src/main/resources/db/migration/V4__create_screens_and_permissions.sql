-- =============================================
-- V4: Catálogo de telas e permissões por usuário
-- =============================================

CREATE TABLE screens (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  label VARCHAR(100) NOT NULL,
  route VARCHAR(100) NOT NULL,
  display_order INT NOT NULL DEFAULT 0
);

CREATE TABLE user_screen (
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  screen_id BIGINT NOT NULL REFERENCES screens(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, screen_id)
);

-- Seed: as 2 telas atuais
INSERT INTO screens (code, label, route, display_order) VALUES
  ('usuarios', 'Usuários', '/usuarios', 1),
  ('permissoes', 'Permissões usuários', '/permissoes-usuarios', 2);

-- Admin tem acesso a tudo
INSERT INTO user_screen (user_id, screen_id)
SELECT u.id, s.id FROM users u, screens s WHERE u.role = 'ADMIN';

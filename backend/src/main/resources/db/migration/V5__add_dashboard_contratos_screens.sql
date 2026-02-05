-- =============================================
-- V5: Adicionar telas Dashboard e Contratos
-- =============================================

-- Adicionar novas telas (dashboard e contratos)
INSERT INTO screens (code, label, route, display_order) VALUES
  ('dashboard', 'Dashboard', '/dashboard', 0),
  ('contratos', 'Contratos', '/contratos', 1);

-- Reordenar as telas existentes
UPDATE screens SET display_order = 2 WHERE code = 'permissoes';
UPDATE screens SET display_order = 3 WHERE code = 'usuarios';

-- Dar acesso às novas telas para todos os admins
INSERT INTO user_screen (user_id, screen_id)
SELECT u.id, s.id 
FROM users u, screens s 
WHERE u.role = 'ADMIN' 
  AND s.code IN ('dashboard', 'contratos')
  AND NOT EXISTS (
    SELECT 1 FROM user_screen us 
    WHERE us.user_id = u.id AND us.screen_id = s.id
  );

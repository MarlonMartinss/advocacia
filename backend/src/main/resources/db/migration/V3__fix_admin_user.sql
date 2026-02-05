-- =============================================
-- V3: Limpar usuário admin para recriação
-- =============================================

-- Remove o usuário admin com senha inválida para que o DataInitializer crie corretamente
DELETE FROM users WHERE username = 'admin';

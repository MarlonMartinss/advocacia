-- =============================================
-- V8: Criar tabela de anexos de contratos
-- =============================================

CREATE TABLE contrato_anexos (
    id BIGSERIAL PRIMARY KEY,
    contrato_id BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE,
    nome_original VARCHAR(255) NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_mime VARCHAR(100),
    tamanho BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para buscar anexos por contrato
CREATE INDEX idx_contrato_anexos_contrato_id ON contrato_anexos(contrato_id);

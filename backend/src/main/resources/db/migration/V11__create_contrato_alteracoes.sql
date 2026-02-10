-- =============================================
-- V11: Histórico de alterações de contratos
-- =============================================

CREATE TABLE IF NOT EXISTS contrato_alteracoes (
    id BIGSERIAL PRIMARY KEY,
    contrato_id BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE,
    username VARCHAR(120) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT now(),
    changes JSONB NOT NULL
);

CREATE INDEX idx_contrato_alteracoes_contrato ON contrato_alteracoes(contrato_id);
CREATE INDEX idx_contrato_alteracoes_contrato_date ON contrato_alteracoes(contrato_id, changed_at DESC);

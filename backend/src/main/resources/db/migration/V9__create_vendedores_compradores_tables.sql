-- =============================================
-- V9: Criar tabelas de vendedores e compradores
--     (suporte a multiplos por contrato)
-- =============================================

CREATE TABLE IF NOT EXISTS contrato_vendedores (
    id BIGSERIAL PRIMARY KEY,
    contrato_id BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL DEFAULT 0,

    -- Dados do vendedor
    nome VARCHAR(200),
    documento VARCHAR(20),
    email VARCHAR(150),
    telefone VARCHAR(20),
    endereco VARCHAR(500),

    -- Socio Administrador (preenchido somente quando documento eh CNPJ)
    socio_nome VARCHAR(200),
    socio_cpf VARCHAR(14),
    socio_nacionalidade VARCHAR(100),
    socio_profissao VARCHAR(100),
    socio_estado_civil VARCHAR(50),
    socio_regime_bens VARCHAR(100),
    socio_rg VARCHAR(20),
    socio_cnh VARCHAR(20),
    socio_email VARCHAR(150),
    socio_telefone VARCHAR(20),
    socio_endereco VARCHAR(500)
);

CREATE INDEX idx_contrato_vendedores_contrato ON contrato_vendedores(contrato_id);

CREATE TABLE IF NOT EXISTS contrato_compradores (
    id BIGSERIAL PRIMARY KEY,
    contrato_id BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL DEFAULT 0,

    -- Dados do comprador
    nome VARCHAR(200),
    documento VARCHAR(20),
    nacionalidade VARCHAR(100),
    profissao VARCHAR(100),
    estado_civil VARCHAR(50),
    regime_bens VARCHAR(100),
    rg VARCHAR(20),
    cnh VARCHAR(20),
    email VARCHAR(150),
    telefone VARCHAR(20),
    endereco VARCHAR(500),

    -- Conjuge (preenchido somente quando estado_civil = Casado ou Uniao Estavel)
    conjuge_nome VARCHAR(200),
    conjuge_cpf VARCHAR(14),
    conjuge_nacionalidade VARCHAR(100),
    conjuge_profissao VARCHAR(100),
    conjuge_rg VARCHAR(20)
);

CREATE INDEX idx_contrato_compradores_contrato ON contrato_compradores(contrato_id);

-- Migrar dados existentes da tabela contratos para as novas tabelas
INSERT INTO contrato_vendedores (contrato_id, ordem, nome, documento, email, telefone, endereco,
    socio_nome, socio_cpf, socio_nacionalidade, socio_profissao, socio_estado_civil,
    socio_regime_bens, socio_rg, socio_cnh, socio_email, socio_telefone, socio_endereco)
SELECT id, 0, vendedor_nome, vendedor_cnpj, vendedor_email, vendedor_telefone, vendedor_endereco,
    socio_nome, socio_cpf, socio_nacionalidade, socio_profissao, socio_estado_civil,
    socio_regime_bens, socio_rg, socio_cnh, socio_email, socio_telefone, socio_endereco
FROM contratos
WHERE vendedor_nome IS NOT NULL AND vendedor_nome <> '';

INSERT INTO contrato_compradores (contrato_id, ordem, nome, documento, nacionalidade, profissao,
    estado_civil, regime_bens, rg, cnh, email, telefone, endereco,
    conjuge_nome, conjuge_cpf, conjuge_nacionalidade, conjuge_profissao, conjuge_rg)
SELECT id, 0, comprador_nome, comprador_cpf, comprador_nacionalidade, comprador_profissao,
    comprador_estado_civil, comprador_regime_bens, comprador_rg, comprador_cnh,
    comprador_email, comprador_telefone, comprador_endereco,
    conjuge_nome, conjuge_cpf, conjuge_nacionalidade, conjuge_profissao, conjuge_rg
FROM contratos
WHERE comprador_nome IS NOT NULL AND comprador_nome <> '';

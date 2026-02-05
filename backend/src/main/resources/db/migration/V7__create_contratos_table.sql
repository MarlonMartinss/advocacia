-- =============================================
-- V7: Criar tabela de contratos
-- =============================================

CREATE TABLE IF NOT EXISTS contratos (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    pagina_atual INTEGER DEFAULT 1,
    
    -- Página 1: Vendedor Pessoa Jurídica
    vendedor_nome VARCHAR(200),
    vendedor_cnpj VARCHAR(20),
    vendedor_email VARCHAR(150),
    vendedor_telefone VARCHAR(20),
    vendedor_endereco VARCHAR(500),
    
    -- Página 1: Sócio Administrador
    socio_nome VARCHAR(200),
    socio_nacionalidade VARCHAR(100),
    socio_profissao VARCHAR(100),
    socio_estado_civil VARCHAR(50),
    socio_regime_bens VARCHAR(100),
    socio_cpf VARCHAR(14),
    socio_rg VARCHAR(20),
    socio_cnh VARCHAR(20),
    socio_email VARCHAR(150),
    socio_telefone VARCHAR(20),
    socio_endereco VARCHAR(500),
    
    -- Página 2: Comprador
    comprador_nome VARCHAR(200),
    comprador_nacionalidade VARCHAR(100),
    comprador_profissao VARCHAR(100),
    comprador_estado_civil VARCHAR(50),
    comprador_regime_bens VARCHAR(100),
    comprador_cpf VARCHAR(14),
    comprador_rg VARCHAR(20),
    comprador_cnh VARCHAR(20),
    comprador_email VARCHAR(150),
    comprador_telefone VARCHAR(20),
    comprador_endereco VARCHAR(500),
    
    -- Página 2: Cônjuge/Convivente
    conjuge_nome VARCHAR(200),
    conjuge_nacionalidade VARCHAR(100),
    conjuge_profissao VARCHAR(100),
    conjuge_cpf VARCHAR(14),
    conjuge_rg VARCHAR(20),
    
    -- Página 3: Imóvel Objeto do Negócio
    imovel_matricula VARCHAR(50),
    imovel_livro VARCHAR(50),
    imovel_oficio VARCHAR(100),
    imovel_proprietario VARCHAR(200),
    imovel_momento_posse VARCHAR(200),
    imovel_prazo_transferencia VARCHAR(200),
    imovel_prazo_escritura VARCHAR(200),
    imovel_descricao TEXT,
    
    -- Página 3: Imóvel Dado em Permuta
    permuta_imovel_matricula VARCHAR(50),
    permuta_imovel_livro VARCHAR(50),
    permuta_imovel_oficio VARCHAR(100),
    permuta_imovel_proprietario VARCHAR(200),
    permuta_imovel_momento_posse VARCHAR(200),
    permuta_imovel_prazo_transferencia VARCHAR(200),
    permuta_imovel_prazo_escritura VARCHAR(200),
    permuta_imovel_descricao TEXT,
    
    -- Página 3: Veículo Dado em Permuta
    veiculo_marca VARCHAR(100),
    veiculo_ano VARCHAR(10),
    veiculo_modelo VARCHAR(100),
    veiculo_placa VARCHAR(10),
    veiculo_chassi VARCHAR(50),
    veiculo_cor VARCHAR(50),
    veiculo_motor VARCHAR(50),
    veiculo_renavam VARCHAR(20),
    veiculo_data_entrega DATE,
    
    -- Página 4: Negócio
    negocio_valor_total DECIMAL(15, 2),
    negocio_valor_entrada DECIMAL(15, 2),
    negocio_forma_pagamento VARCHAR(500),
    negocio_num_parcelas INTEGER,
    negocio_valor_parcela DECIMAL(15, 2),
    negocio_vencimentos VARCHAR(500),
    negocio_valor_imovel_permuta DECIMAL(15, 2),
    negocio_valor_veiculo_permuta DECIMAL(15, 2),
    negocio_valor_financiamento DECIMAL(15, 2),
    negocio_prazo_pagamento VARCHAR(200),
    
    -- Página 4: Conta Bancária
    conta_titular VARCHAR(200),
    conta_banco VARCHAR(100),
    conta_agencia VARCHAR(20),
    conta_pix VARCHAR(200),
    
    -- Página 4: Honorários
    honorarios_valor DECIMAL(15, 2),
    honorarios_forma_pagamento VARCHAR(200),
    honorarios_data_pagamento DATE,
    
    -- Página 4: Observações e Assinaturas
    observacoes TEXT,
    data_contrato DATE,
    assinatura_corretor VARCHAR(200),
    assinatura_agenciador VARCHAR(200),
    assinatura_gestor VARCHAR(200),
    
    -- Auditoria
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Índice para buscar contratos por status
CREATE INDEX idx_contratos_status ON contratos(status);

-- Data da 1ª parcela e lista de parcelas (numero, vencimento, valor) para parcelamento do saldo a pagar
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS negocio_data_primeira_parcela DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS negocio_parcelas JSONB;

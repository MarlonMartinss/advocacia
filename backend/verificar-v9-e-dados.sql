-- Rode este script no PostgreSQL (psql, DBeaver, etc.) para verificar:
-- 1) Se a migração V9 já executou
-- 2) Se existem vendedores e compradores nas novas tabelas

-- 1) Histórico Flyway (deve listar V9__create_vendedores_compradores_tables)
SELECT version, description, installed_on, success
FROM flyway_schema_history
WHERE version >= '9'
ORDER BY installed_rank;

-- 2) Contagem de registros nas novas tabelas
SELECT 'contrato_vendedores' AS tabela, COUNT(*) AS total FROM contrato_vendedores
UNION ALL
SELECT 'contrato_compradores', COUNT(*) FROM contrato_compradores;

-- 3) Contratos e quantos vendedores/compradores cada um tem
SELECT c.id AS contrato_id,
       (SELECT COUNT(*) FROM contrato_vendedores v WHERE v.contrato_id = c.id) AS qtd_vendedores,
       (SELECT COUNT(*) FROM contrato_compradores co WHERE co.contrato_id = c.id) AS qtd_compradores
FROM contratos c
ORDER BY c.id;

-- 4) Amostra: primeiros vendedores e compradores (nomes)
SELECT 'vendedor' AS tipo, id, contrato_id, nome FROM contrato_vendedores ORDER BY contrato_id, ordem LIMIT 10;
SELECT 'comprador' AS tipo, id, contrato_id, nome FROM contrato_compradores ORDER BY contrato_id, ordem LIMIT 10;

# Script de verificação do histórico (manual)

Quando não puder rodar os testes automatizados, use estes passos para validar o fluxo.

## 1. Backend

1. Inicie o backend (Docker ou `.\mvnw.cmd spring-boot:run`).
2. Edite um contrato (ex.: altere o valor total), salve.
3. No console do backend, confira:
   - `[HISTORICO] UPDATE contrato id=X ...`
   - `Auditoria contrato X: N alteração(ões)`
   - `[HISTORICO] Histórico gravado contratoId=X`
4. Abra o histórico desse contrato na tela. No console:
   - `[HISTORICO] GET historico contratoId=X size=N` com N >= 1.

## 2. Banco (Postgres)

```sql
SELECT id, contrato_id, username, changed_at, left(changes::text, 80) AS changes_preview
FROM contrato_alteracoes
ORDER BY changed_at DESC
LIMIT 10;
```

Deve existir pelo menos uma linha com `contrato_id` igual ao contrato que você editou.

## 3. Frontend (DevTools Console)

1. Abra o histórico de um contrato.
2. No console do navegador deve aparecer:
   - `[HISTORICO] abrirHistorico contractId= X`
   - `[HISTORICO] GET historico contractId= X resposta size= N bruto= [...]`

Se `size=0` e no backend apareceu "Histórico gravado", confira se o GET está usando o mesmo `id` (aba Network: URL do GET).

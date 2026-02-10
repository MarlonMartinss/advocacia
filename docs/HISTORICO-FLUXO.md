# Histórico de alterações – Mapa do fluxo e diagnóstico

## 1. Mapa do código (arquivos e funções)

### Tela / modal / botão "Histórico de alterações"

| Onde | Arquivo | Função / elemento |
|------|---------|-------------------|
| Botão que abre o histórico | `frontend/src/app/components/contratos/contratos.component.html` | Botão `(click)="abrirHistorico(contrato)"` (linha ~84) |
| Modal do histórico | `frontend/src/app/components/contratos/contratos.component.html` | `*ngIf="showHistoricoModal"`, título "Histórico de alterações" (linhas ~776-827) |
| Estado e abertura | `frontend/src/app/components/contratos/contratos.component.ts` | `historicoAlteracoes`, `abrirHistorico(contrato)`, `fecharHistorico()` (linhas ~51-54, 720-743) |

### Endpoint(s) GET do histórico

| Camada | Arquivo | Função |
|--------|---------|--------|
| Controller | `backend/src/main/java/com/advocacia/controller/ContratoController.java` | `getHistorico(@PathVariable Long id)` → `GET /api/contratos/{id}/historico` (linhas 55-59) |
| Service | `backend/src/main/java/com/advocacia/service/ContratoAuditService.java` | `getHistorico(Long contratoId)` (linha 143) |
| Repository | `backend/src/main/java/com/advocacia/repository/ContratoAlteracaoRepository.java` | `findByContratoIdOrderByChangedAtDesc(Long contratoId)` (linha 11) |
| Frontend | `frontend/src/app/services/contrato.service.ts` | `getHistorico(id: number)` → `GET ${apiUrl}/${id}/historico` (linhas 192-193) |

### Endpoint(s) UPDATE do contrato (PUT/PATCH)

| Camada | Arquivo | Função |
|--------|---------|--------|
| Controller | `backend/src/main/java/com/advocacia/controller/ContratoController.java` | `update(@PathVariable Long id, @RequestBody ContratoRequest request)` → `PUT /api/contratos/{id}` (linhas 36-39) |
| Service | `backend/src/main/java/com/advocacia/service/ContratoService.java` | `update(Long id, ContratoRequest request)` (linhas 53-82) |
| Frontend | `frontend/src/app/components/contratos/contratos.component.ts` | `saveDraft()` → `contratoService.update(this.contratoId, request)` quando `this.contratoId` existe (linhas 519-523) |
| Frontend | `frontend/src/app/services/contrato.service.ts` | `update(id, data)` → `PUT ${apiUrl}/${id}` (linhas 170-171) |

### Onde deveria ocorrer a criação do histórico

| Onde | Arquivo | Função |
|------|---------|--------|
| Criação do histórico (update completo) | `backend/src/main/java/com/advocacia/service/ContratoService.java` | `update()` chama `auditService.recordChanges(id, before, after)` (linha 67) |
| Criação do histórico (só vendedores) | `backend/src/main/java/com/advocacia/service/ContratoService.java` | `updateVendedores()` chama `auditService.recordChanges(id, before, after)` (linha 162) |
| Criação do histórico (só compradores) | `backend/src/main/java/com/advocacia/service/ContratoService.java` | `updateCompradores()` chama `auditService.recordChanges(id, before, after)` (linha 210) |
| Implementação da gravação | `backend/src/main/java/com/advocacia/service/ContratoAuditService.java` | `recordChanges(Long contratoId, Object before, Object after)` (linhas 58-116): diff, fallback manual, fallback em exceção, `alteracaoRepository.save(alteracao)` |

---

## 2. Schema da tabela do histórico (persistência)

**Tabela:** `contrato_alteracoes` (Flyway: `V11__create_contrato_alteracoes.sql`)

| Coluna | Tipo | Observação |
|--------|------|------------|
| id | BIGSERIAL PRIMARY KEY | |
| contrato_id | BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE | FK para o contrato |
| username | VARCHAR(120) NOT NULL | Quem alterou |
| changed_at | TIMESTAMP NOT NULL DEFAULT now() | Quando |
| changes | JSONB NOT NULL | Array JSON de `{ path, oldValue, newValue }` |

**Índices:** `contrato_id`, `(contrato_id, changed_at DESC)` para a consulta do histórico.

**Entidade JPA:** `backend/src/main/java/com/advocacia/entity/ContratoAlteracao.java` (campo `changes` como `String` com `columnDefinition = "jsonb"`).

---

## 3. Verificação UPDATE (entrada)

- **Frontend:** Ao salvar rascunho com contrato existente, `saveDraft()` monta `request` com `...formData`, `vendedores`, `compradores`, `paginaAtual`, `negocioValorTotal` explícito e chama `contratoService.update(this.contratoId, request)`.
- **Backend:** `ContratoController.update(id, request)` chama `contratoService.update(id, request)`. Em `ContratoService.update()`: carrega contrato, faz `before = toResponse(contrato)`, aplica `updateContratoFromRequest(contrato, request)`, `save(contrato)`, `after = toResponse(contrato)`, depois `auditService.recordChanges(id, before, after)`.

Logs temporários adicionados (ver código):
- Em `ContratoService.update`: log com `id`, resumo do payload (ex.: `negocioValorTotal`), `before`/`after` (ex.: valor total).
- Em `ContratoAuditService.recordChanges`: já existem logs de "Auditoria contrato X: N alteração(ões)", "Diff vazio...", "Teste definitivo...", "Histórico gravado" (após save).
- Em `ContratoAuditService.getHistorico`: log com `contratoId` e quantidade retornada.

---

## 4. Verificação da consulta do histórico (leitura)

- **Endpoint:** `GET /api/contratos/{id}/historico` → `auditService.getHistorico(id)`.
- **Query:** `findByContratoIdOrderByChangedAtDesc(contratoId)` → `SELECT * FROM contrato_alteracoes WHERE contrato_id = ? ORDER BY changed_at DESC`. Sem paginação; retorna toda a lista.
- **Resposta:** Lista de `ContratoAlteracaoResponse` (id, contratoId, username, changedAt, changes[] com path/oldValue/newValue).

---

## 5. Verificação do front (renderização)

- **Chamada:** `abrirHistorico(contrato)` usa `contrato.id` e chama `getHistorico(contrato.id)`.
- **Resposta:** Preenche `historicoAlteracoes`; se `length === 0` mostra "Nenhuma alteração registrada"; senão exibe lista com `alteracao.changedAt`, `alteracao.username`, `alteracao.changes`.
- **Cache:** Não usa React Query/SWR; chamada direta a cada abertura do modal.
- Logs temporários no front: em `abrirHistorico` (contractId, tamanho da resposta) e em `saveDraft` (contratoId, se chamou update).

---

## 6. Causa raiz (resumo)

O histórico não aparecia porque: (1) quando `recordChanges` lançava exceção (ex.: em `valueToTree(before/after)`), o código relançava e o `ContratoService` só gravava fallback para `negocioValorTotal`; (2) quando o diff e o fallback manual vinham vazios, o código retornava sem gravar nada. Assim, em vários fluxos (erro de serialização ou diff sempre vazio) nenhuma linha era inserida em `contrato_alteracoes`. Além disso, se `entity.getChanges()` fosse null no GET, o `toResponse` poderia falhar e retornar 500.

---

## 7. Fix já aplicado (diff conceitual)

1. **ContratoAuditService.recordChanges():**
   - Quando diff e fallback manual estão vazios: grava uma linha de "teste definitivo" (`alteracao` / "registro de auditoria") em vez de só return.
   - No catch: não relançar; gravar uma linha de fallback com `changeEntry("alteracao", "erro ao gerar diff", msg)` e fazer save; se esse save falhar, só log.

2. **ContratoAuditService.toResponse():** Tratar `entity.getChanges()` null ou em branco (usar `fieldChanges = List.of()`), evitando NPE/500 no GET.

3. **Logs de diagnóstico** (temporários): em `ContratoService.update`, `ContratoAuditService.recordChanges` (após save) e `getHistorico`, e no front em `saveDraft` e `abrirHistorico`.

---

## 8. Como validar (passo a passo)

1. Reiniciar o backend (rebuild/restart do container ou `mvn spring-boot:run`).
2. Logar na aplicação, abrir um contrato existente, alterar um campo (ex.: valor total) e clicar em Salvar (rascunho).
3. No console do backend: ver "UPDATE contrato id=...", "Auditoria contrato X: ...", e "Histórico gravado contratoId=X" (ou "Teste definitivo..." / "Fallback...").
4. Na tela de contratos, clicar no ícone de relógio (Histórico de alterações) do mesmo contrato.
5. Ver no backend: "GET historico contratoId=X size=...".
6. No modal: deve aparecer pelo menos uma entrada no histórico.
7. (Opcional) No banco: `SELECT id, contrato_id, username, changed_at FROM contrato_alteracoes ORDER BY changed_at DESC LIMIT 10;` deve listar linhas com o `contrato_id` correto.

---

## 9. Testes

- **Teste 1:** "Ao chamar recordChanges com before/after diferentes, save é chamado com contratoId e changes preenchidos" — `ContratoAuditServiceTest.recordChanges_deveChamarSave_comContratoIdEChanges`.
- **Teste 2:** "GET histórico retorna registros do contrato" — `ContratoAuditServiceTest.getHistorico_retornaRegistrosDoContrato`.

**Arquivo:** `backend/src/test/java/com/advocacia/service/ContratoAuditServiceTest.java`

**Comando (na pasta backend):**

```bash
.\mvnw.cmd test -Dtest=ContratoAuditServiceTest
```

# Causa raiz: mudanças extras no histórico quando apenas um campo é editado

## Resumo

Ao editar **apenas um campo** (ex.: nome do vendedor), o histórico exibe **muitas alterações** (IDs, ordem, às vezes todos os campos da lista). A causa é a **recriação das listas** de vendedores/compradores no update e o **diff** que compara objetos que incluem `id` e `ordem` em todos os níveis.

---

## 1. Update recria listas em vez de atualizar in place

### Trecho exato

**Arquivo:** `ContratoService.java`  
**Método:** `updateContratoFromRequest(Contrato contrato, ContratoRequest request)`

```java
// Linhas 237-264
if (request.getVendedores() != null) {
    contrato.getVendedores().clear();   // ← Remove todos os vendedores existentes
    for (int i = 0; i < request.getVendedores().size(); i++) {
        VendedorRequest vr = request.getVendedores().get(i);
        ContratoVendedor vendedor = ContratoVendedor.builder()
                .contrato(contrato)
                .ordem(i)
                .nome(vr.getNome())
                // ... outros campos
                .build();   // ← SEM .id() — nova entidade, ID gerado no save
        contrato.getVendedores().add(vendedor);
    }
}
```

O mesmo padrão é usado para **compradores** (linhas 266-291): `clear()` + novas entidades sem `id`.

**Efeito:** O ORM persiste as novas entidades e **atribui novos IDs**. Os vendedores/compradores “antigos” são removidos (orphanRemoval ou delete em cascade, conforme mapeamento).

---

## 2. Snapshot “before” vs “after” inclui IDs e ordem

**Arquivo:** `ContratoService.java`  
**Método:** `update(Long id, ContratoRequest request)`

```java
// Linhas 60-68
ContratoResponse before = toResponse(contrato);   // vendedores[0].id = 123, vendedores[0].nome = "Marlon"
updateContratoFromRequest(contrato, request);     // clear + novos vendedores (sem id)
contrato = contratoRepository.save(contrato);     // persiste; vendedores[0] recebe id = 456
ContratoResponse after = toResponse(contrato);   // vendedores[0].id = 456, vendedores[0].nome = "Marlon"
auditService.recordChanges(id, before, after);    // diff compara before x after
```

**Arquivo:** `ContratoService.java`  
**Método:** `toVendedorResponse(ContratoVendedor v)`

```java
// Linhas 418-435
return VendedorResponse.builder()
        .id(v.getId())      // ← Incluído no JSON do snapshot
        .ordem(v.getOrdem()) // ← Incluído
        .nome(v.getNome())
        // ...
```

Ou seja, o “before” tem `vendedores[0].id = 123` e o “after” tem `vendedores[0].id = 456`, mesmo quando só o nome foi alterado (ou nem isso). O diff passa a registrar **mudança de id** (e de ordem, se mudar).

---

## 3. Diff não ignora “id” e “ordem” em paths aninhados

**Arquivo:** `ContratoAuditService.java`  
**Método:** `computeDiff(String prefix, JsonNode before, JsonNode after, List<Map<String,String>> changes)`

```java
// Linhas 225-228
for (String key : allKeys) {
    if (prefix.isEmpty() && IGNORED_FIELDS.contains(key)) continue;  // ← Só ignora no ROOT
    String path = prefix.isEmpty() ? key : prefix + "." + key;
    computeDiff(path, before.get(key), after.get(key), changes);
}
```

`IGNORED_FIELDS` = `createdAt`, `updatedAt`, `id`, `status`, `paginaAtual` — mas a condição é **só quando `prefix.isEmpty()`**. Para `vendedores[0].id` o prefix é `vendedores[0]`, então **não está vazio** e o campo `id` **não é ignorado**. Resultado: o diff gera entrada para `vendedores[0].id` (oldValue=123, newValue=456) e, dependendo do caso, para `vendedores[0].ordem` e outros campos técnicos.

---

## 4. Efeitos colaterais possíveis

- **paginaAtual:** em `updateContratoFromRequest`, se o request enviar `paginaAtual`, ele é aplicado (linha 235). Isso gera mudança no diff.
- **status:** não é alterado no `update()` genérico; só em `finalizar()`. O root `status` já está em `IGNORED_FIELDS` no nível raiz (e continua sendo ignorado só aí).
- **Normalização:** `sanitizeDigits` em documento pode fazer “123.456” → “123456”, gerando “mudança” mesmo quando o usuário não alterou o valor exibido.

---

## Conclusão

| Causa | Onde |
|-------|------|
| Listas recriadas (clear + novas entidades) | `ContratoService.updateContratoFromRequest` (vendedores/compradores) |
| Novos IDs após save | ORM ao persistir entidades sem `id` |
| Snapshot inclui id/ordem | `ContratoService.toVendedorResponse` / `toCompradorResponse` |
| Diff não ignora id/ordem em paths aninhados | `ContratoAuditService.computeDiff` (ignora só quando `prefix.isEmpty()`) |

Por isso, ao editar **só o nome do vendedor**, o histórico mostra mudanças em **id**, **ordem** e, em alguns casos, em vários outros campos da mesma entidade. A correção envolve: (1) ignorar no diff os campos técnicos em **qualquer** path e (2) sanitizar/filtrar o log de auditoria antes de exibir (e, opcionalmente, atualizar vendedores/compradores in place em vez de recriar).

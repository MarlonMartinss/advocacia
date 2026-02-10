package com.advocacia.service;

import com.advocacia.dto.ContratoAlteracaoResponse;
import com.advocacia.entity.ContratoAlteracao;
import com.advocacia.repository.ContratoAlteracaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContratoAuditService {

    private final ContratoAlteracaoRepository alteracaoRepository;
    private final TransactionTemplate transactionTemplateRequiresNew;

    public ContratoAuditService(ContratoAlteracaoRepository alteracaoRepository,
                               @Qualifier("transactionTemplateRequiresNew") TransactionTemplate transactionTemplateRequiresNew) {
        this.alteracaoRepository = alteracaoRepository;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** Ignorados apenas no nível raiz do contrato. */
    private static final Set<String> IGNORED_FIELDS_ROOT = Set.of(
            "createdAt", "updatedAt", "id", "status", "paginaAtual"
    );

    /** Ignorados em qualquer nível (ex.: vendedores[0].id, vendedores[0].ordem). */
    private static final Set<String> IGNORED_FIELDS_ANY_LEVEL = Set.of(
            "createdAt", "updatedAt", "id", "ordem", "status", "paginaAtual"
    );

    /** Campos escalares do contrato usados no fallback quando o diff recursivo retorna vazio. */
    private static final List<String> SCALAR_AUDIT_FIELDS = Arrays.asList(
            "imovelMatricula", "imovelLivro", "imovelOficio", "imovelProprietario", "imovelMomentoPosse",
            "imovelPrazoTransferencia", "imovelPrazoEscritura", "imovelDescricao",
            "permutaImovelMatricula", "permutaImovelLivro", "permutaImovelOficio", "permutaImovelProprietario",
            "permutaImovelMomentoPosse", "permutaImovelPrazoTransferencia", "permutaImovelPrazoEscritura", "permutaImovelDescricao",
            "veiculoMarca", "veiculoAno", "veiculoModelo", "veiculoPlaca", "veiculoChassi", "veiculoCor",
            "veiculoMotor", "veiculoRenavam", "veiculoDataEntrega", "veiculoKm",
            "negocioValorTotal", "negocioValorEntrada", "negocioFormaPagamento", "negocioNumParcelas",
            "negocioValorParcela", "negocioVencimentos", "negocioValorImovelPermuta", "negocioValorVeiculoPermuta",
            "negocioValorFinanciamento", "negocioPrazoPagamento",
            "contaTitular", "contaBanco", "contaAgencia", "contaPix",
            "honorariosValor", "honorariosFormaPagamento", "honorariosDataPagamento",
            "observacoes", "dataContrato", "assinaturaCorretor", "assinaturaAgenciador", "assinaturaGestor"
    );

    /**
     * Compara before e after (qualquer objeto), gera diff e persiste se houver mudanças.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordChanges(Long contratoId, Object before, Object after) {
        try {
            JsonNode beforeNode = MAPPER.valueToTree(before);
            JsonNode afterNode = MAPPER.valueToTree(after);

            List<Map<String, String>> changes = new ArrayList<>();
            computeDiff("", beforeNode, afterNode, changes);

            log.info("Auditoria contrato {}: {} alteração(ões) detectada(s)", contratoId, changes.size());

            if (changes.isEmpty()) {
                String beforeVal = beforeNode.has("negocioValorTotal") ? nodeToString(beforeNode.get("negocioValorTotal")) : "ausente";
                String afterVal = afterNode.has("negocioValorTotal") ? nodeToString(afterNode.get("negocioValorTotal")) : "ausente";
                String beforeObs = beforeNode.has("observacoes") ? nodeToString(beforeNode.get("observacoes")) : "ausente";
                String afterObs = afterNode.has("observacoes") ? nodeToString(afterNode.get("observacoes")) : "ausente";
                log.info("Diff vazio para contrato {} - negocioValorTotal before: {}, after: {}; observacoes before: {}, after: {}",
                        contratoId, beforeVal, afterVal, beforeObs, afterObs);

                List<Map<String, String>> manualChanges = buildManualScalarChanges(beforeNode, afterNode);
                if (!manualChanges.isEmpty()) {
                    log.info("Fallback manual: {} campo(s) alterado(s) para contrato {}", manualChanges.size(), contratoId);
                    changes = manualChanges;
                } else {
                    // Teste definitivo: sempre gravar uma linha para validar tabela e leitura (temporário para diagnóstico)
                    log.info("Teste definitivo: gravando registro de auditoria para contrato {} (diff e manual vazios)", contratoId);
                    changes = List.of(changeEntry("alteracao", "sem diff", "registro de auditoria"));
                }
            }

            String username = getCurrentUsername();

            ContratoAlteracao alteracao = ContratoAlteracao.builder()
                    .contratoId(contratoId)
                    .username(username)
                    .changedAt(LocalDateTime.now())
                    .changes(changes)
                    .build();

            log.info("[HISTORICO] antes do save: contratoId={}, username={}, changes size={}", contratoId, username, changes.size());
            ContratoAlteracao saved = alteracaoRepository.save(alteracao);
            log.info("[HISTORICO] depois do save: id gerado={}", saved != null ? saved.getId() : null);
        } catch (Exception e) {
            log.error("Erro ao registrar alteração do contrato {}: {}", contratoId, e.getMessage(), e);
            saveFallbackInNewTransaction(contratoId, "alteracao", "erro ao gerar diff", e.getMessage() != null ? e.getMessage() : "erro");
        }
    }

    /** Persiste uma linha de fallback em transação nova (REQUIRES_NEW) para não ser revertida pelo rollback da transação que falhou. */
    private void saveFallbackInNewTransaction(Long contratoId, String path, String oldValue, String newValue) {
        try {
            transactionTemplateRequiresNew.executeWithoutResult(status -> {
                String username = getCurrentUsername();
                List<Map<String, String>> fallbackChanges = List.of(changeEntry(path, oldValue, newValue));
                ContratoAlteracao fallback = ContratoAlteracao.builder()
                        .contratoId(contratoId)
                        .username(username)
                        .changedAt(LocalDateTime.now())
                        .changes(fallbackChanges)
                        .build();
                log.info("[HISTORICO] fallback antes do save: contratoId={}, username={}, changes size=1", contratoId, username);
                ContratoAlteracao savedFallback = alteracaoRepository.save(fallback);
                log.info("[HISTORICO] fallback depois do save: id gerado={}", savedFallback != null ? savedFallback.getId() : null);
            });
        } catch (Exception fallbackEx) {
            log.warn("Fallback de auditoria também falhou para contrato {}: {}", contratoId, fallbackEx.getMessage());
        }
    }

    /**
     * Registra uma mudança simples (ex.: finalização).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSimpleChange(Long contratoId, String path, String oldValue, String newValue) {
        try {
            List<Map<String, String>> changes = List.of(changeEntry(path, oldValue, newValue));

            String username = getCurrentUsername();

            ContratoAlteracao alteracao = ContratoAlteracao.builder()
                    .contratoId(contratoId)
                    .username(username)
                    .changedAt(LocalDateTime.now())
                    .changes(changes)
                    .build();

            alteracaoRepository.save(alteracao);
        } catch (Exception e) {
            log.error("Erro ao registrar alteração simples do contrato {}: {}", contratoId, e.getMessage(), e);
        }
    }

    public List<ContratoAlteracaoResponse> getHistorico(Long contratoId) {
        List<ContratoAlteracaoResponse> list = alteracaoRepository.findByContratoIdOrderByChangedAtDesc(contratoId).stream()
                .map(this::toResponse)
                .map(this::sanitizeAndEnrichResponse)
                .collect(Collectors.toList());
        log.info("[HISTORICO] GET historico contratoId={} size={}", contratoId, list.size());
        return list;
    }

    /**
     * Sanitiza alterações (remove técnicas e old==new) e preenche label/displayOld/displayNew para UX.
     */
    public List<ContratoAlteracaoResponse.FieldChange> sanitizeAuditChanges(List<ContratoAlteracaoResponse.FieldChange> changes) {
        if (changes == null) return List.of();
        return changes.stream()
                .filter(c -> isAuditablePath(c.getPath()))
                .filter(c -> !Objects.equals(nullSafe(c.getOldValue()), nullSafe(c.getNewValue())))
                .collect(Collectors.toList());
    }

    /**
     * Rótulo amigável para exibição do path (ex.: "vendedores[0].nome" → "Vendedor - Nome").
     */
    public String labelForPath(String path) {
        if (path == null || path.isBlank()) return path;
        String normalized = path.replaceAll("\\[\\d+\\]", "").replaceAll("^\\.", "");
        if (normalized.startsWith("vendedores.")) return "Vendedor - " + labelForFieldName(normalized.substring("vendedores.".length()));
        if (normalized.startsWith("compradores.")) return "Comprador - " + labelForFieldName(normalized.substring("compradores.".length()));
        return labelForFieldName(normalized);
    }

    /**
     * Valor formatado para exibição (ex.: DRAFT → Rascunho).
     */
    public String formatValue(String path, String value) {
        if (value == null || "null".equals(value) || value.isBlank()) return "(vazio)";
        if ("(removido)".equals(value)) return value;
        if (path != null && path.endsWith("status")) return translateStatus(value);
        return value;
    }

    private ContratoAlteracaoResponse sanitizeAndEnrichResponse(ContratoAlteracaoResponse r) {
        List<ContratoAlteracaoResponse.FieldChange> sanitized = sanitizeAuditChanges(r.getChanges());
        List<ContratoAlteracaoResponse.FieldChange> enriched = sanitized.stream()
                .map(c -> ContratoAlteracaoResponse.FieldChange.builder()
                        .path(c.getPath())
                        .oldValue(c.getOldValue())
                        .newValue(c.getNewValue())
                        .label(labelForPath(c.getPath()))
                        .displayOld(formatValue(c.getPath(), c.getOldValue()))
                        .displayNew(formatValue(c.getPath(), c.getNewValue()))
                        .build())
                .collect(Collectors.toList());
        return ContratoAlteracaoResponse.builder()
                .id(r.getId())
                .contratoId(r.getContratoId())
                .username(r.getUsername())
                .changedAt(r.getChangedAt())
                .changes(enriched)
                .build();
    }

    private boolean isAuditablePath(String path) {
        if (path == null || path.isBlank()) return false;
        String p = path.toLowerCase();
        if (p.endsWith(".id") || p.equals("id")) return false;
        if (p.endsWith(".ordem") || p.equals("ordem")) return false;
        if (p.endsWith(".createdat") || p.contains("createdat")) return false;
        if (p.endsWith(".updatedat") || p.contains("updatedat")) return false;
        if (p.endsWith(".paginaatual") || p.contains("paginaatual")) return false;
        return true;
    }

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("nome", "Nome"), Map.entry("documento", "Documento"), Map.entry("email", "E-mail"),
            Map.entry("telefone", "Telefone"), Map.entry("endereco", "Endereço"),
            Map.entry("socioNome", "Sócio - Nome"), Map.entry("socioCpf", "Sócio - CPF"),
            Map.entry("socioNacionalidade", "Sócio - Nacionalidade"), Map.entry("socioProfissao", "Sócio - Profissão"),
            Map.entry("socioEstadoCivil", "Sócio - Estado Civil"), Map.entry("socioRegimeBens", "Sócio - Regime de Bens"),
            Map.entry("socioRg", "Sócio - RG"), Map.entry("socioCnh", "Sócio - CNH"), Map.entry("socioEmail", "Sócio - E-mail"),
            Map.entry("socioTelefone", "Sócio - Telefone"), Map.entry("socioEndereco", "Sócio - Endereço"),
            Map.entry("nacionalidade", "Nacionalidade"), Map.entry("profissao", "Profissão"), Map.entry("estadoCivil", "Estado Civil"),
            Map.entry("regimeBens", "Regime de Bens"), Map.entry("rg", "RG"), Map.entry("cnh", "CNH"),
            Map.entry("conjugeNome", "Cônjuge - Nome"), Map.entry("conjugeCpf", "Cônjuge - CPF"),
            Map.entry("conjugeNacionalidade", "Cônjuge - Nacionalidade"), Map.entry("conjugeProfissao", "Cônjuge - Profissão"),
            Map.entry("conjugeRg", "Cônjuge - RG"),
            Map.entry("status", "Status"),
            Map.entry("observacoes", "Observações"), Map.entry("negocioValorTotal", "Negócio - Valor Total"),
            Map.entry("negocioValorEntrada", "Negócio - Valor Entrada"), Map.entry("negocioFormaPagamento", "Negócio - Forma de Pagamento"),
            Map.entry("negocioNumParcelas", "Negócio - Nº Parcelas"), Map.entry("negocioValorParcela", "Negócio - Valor Parcela"),
            Map.entry("negocioVencimentos", "Negócio - Vencimentos"), Map.entry("negocioValorImovelPermuta", "Negócio - Valor Imóvel Permuta"),
            Map.entry("negocioValorVeiculoPermuta", "Negócio - Valor Veículo Permuta"), Map.entry("negocioValorFinanciamento", "Negócio - Valor Financiamento"),
            Map.entry("negocioPrazoPagamento", "Negócio - Prazo Pagamento")
    );

    private String labelForFieldName(String fieldName) {
        return FIELD_LABELS.getOrDefault(fieldName, fieldName);
    }

    private String translateStatus(String value) {
        if (value == null) return "(vazio)";
        return switch (value.toUpperCase()) {
            case "DRAFT" -> "Rascunho";
            case "FINAL" -> "Finalizado";
            default -> value;
        };
    }

    private ContratoAlteracaoResponse toResponse(ContratoAlteracao entity) {
        List<ContratoAlteracaoResponse.FieldChange> fieldChanges;
        List<Map<String, String>> rawChanges = entity.getChanges();
        if (rawChanges == null || rawChanges.isEmpty()) {
            fieldChanges = List.of();
        } else {
            fieldChanges = rawChanges.stream()
                    .map(m -> ContratoAlteracaoResponse.FieldChange.builder()
                            .path(m.getOrDefault("path", ""))
                            .oldValue(m.getOrDefault("oldValue", ""))
                            .newValue(m.getOrDefault("newValue", ""))
                            .build())
                    .collect(Collectors.toList());
        }

        return ContratoAlteracaoResponse.builder()
                .id(entity.getId())
                .contratoId(entity.getContratoId())
                .username(entity.getUsername())
                .changedAt(entity.getChangedAt())
                .changes(fieldChanges)
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "sistema";
    }

    /**
     * Comparação manual de campos escalares quando o diff recursivo retorna vazio.
     */
    private List<Map<String, String>> buildManualScalarChanges(JsonNode beforeNode, JsonNode afterNode) {
        List<Map<String, String>> manual = new ArrayList<>();
        if (beforeNode == null || !beforeNode.isObject() || afterNode == null || !afterNode.isObject()) {
            return manual;
        }
        for (String field : SCALAR_AUDIT_FIELDS) {
            String bVal = nodeToString(beforeNode.get(field));
            String aVal = nodeToString(afterNode.get(field));
            if (!bVal.equals(aVal)) {
                manual.add(changeEntry(field, bVal, aVal));
            }
        }
        return manual;
    }

    // ========== DIFF RECURSIVO ==========

    private void computeDiff(String prefix, JsonNode before, JsonNode after, List<Map<String, String>> changes) {
        if (before == null && after == null) return;
        if (before == null) before = MAPPER.nullNode();
        if (after == null) after = MAPPER.nullNode();

        if (before.isObject() && after.isObject()) {
            Set<String> allKeys = new LinkedHashSet<>();
            before.fieldNames().forEachRemaining(allKeys::add);
            after.fieldNames().forEachRemaining(allKeys::add);

            for (String key : allKeys) {
                if (prefix.isEmpty() && IGNORED_FIELDS_ROOT.contains(key)) continue;
                if (IGNORED_FIELDS_ANY_LEVEL.contains(key)) continue;
                String path = prefix.isEmpty() ? key : prefix + "." + key;
                computeDiff(path, before.get(key), after.get(key), changes);
            }
        } else if (before.isArray() && after.isArray()) {
            int maxLen = Math.max(before.size(), after.size());
            for (int i = 0; i < maxLen; i++) {
                String path = prefix + "[" + i + "]";
                JsonNode bElem = i < before.size() ? before.get(i) : null;
                JsonNode aElem = i < after.size() ? after.get(i) : null;

                if (bElem == null && aElem != null) {
                    changes.add(changeEntry(path, "null", nodeToString(aElem)));
                } else if (bElem != null && aElem == null) {
                    changes.add(changeEntry(path, nodeToString(bElem), "(removido)"));
                } else {
                    computeDiff(path, bElem, aElem, changes);
                }
            }
        } else {
            String bVal = nodeToString(before);
            String aVal = nodeToString(after);
            if (!bVal.equals(aVal)) {
                changes.add(changeEntry(prefix, bVal, aVal));
            }
        }
    }

    private String nodeToString(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return "null";
        if (node.isTextual()) return node.asText();
        if (node.isNumber()) return node.asText();
        if (node.isBoolean()) return node.asText();
        return node.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "null" : value;
    }

    private Map<String, String> changeEntry(String path, String oldValue, String newValue) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("path", nullSafe(path));
        entry.put("oldValue", nullSafe(oldValue));
        entry.put("newValue", nullSafe(newValue));
        return entry;
    }
}

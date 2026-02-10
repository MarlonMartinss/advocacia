package com.advocacia.service;

import com.advocacia.dto.ContratoAlteracaoResponse;
import com.advocacia.entity.ContratoAlteracao;
import com.advocacia.repository.ContratoAlteracaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes do histórico de alterações de contrato.
 * - Ao chamar recordChanges, deve ser criado pelo menos 1 registro (save chamado).
 * - GET histórico retorna registros do contrato (findByContratoIdOrderByChangedAtDesc).
 */
@ExtendWith(MockitoExtension.class)
class ContratoAuditServiceTest {

    @Mock
    private ContratoAlteracaoRepository alteracaoRepository;

    @Mock
    private TransactionTemplate transactionTemplateRequiresNew;

    @InjectMocks
    private ContratoAuditService auditService;

    @Test
    void recordChanges_deveChamarSave_comContratoIdEChanges() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", null));

        Object before = new DummyResponse("10");
        Object after = new DummyResponse("20");

        when(alteracaoRepository.save(any(ContratoAlteracao.class))).thenAnswer(inv -> {
            ContratoAlteracao a = inv.getArgument(0);
            return ContratoAlteracao.builder().id(99L).contratoId(a.getContratoId()).username(a.getUsername()).changedAt(a.getChangedAt()).changes(a.getChanges()).build();
        });

        auditService.recordChanges(1L, before, after);

        ArgumentCaptor<ContratoAlteracao> captor = ArgumentCaptor.forClass(ContratoAlteracao.class);
        verify(alteracaoRepository, atLeastOnce()).save(captor.capture());

        ContratoAlteracao saved = captor.getValue();
        assertThat(saved.getContratoId()).isEqualTo(1L);
        assertThat(saved.getUsername()).isNotNull();
        assertThat(saved.getChanges()).isNotEmpty();
        assertThat(saved.getChanges().stream().anyMatch(m ->
                "negocioValorTotal".equals(m.get("path")) && "10".equals(m.get("oldValue")) && "20".equals(m.get("newValue"))))
                .isTrue();
    }

    @Test
    void getHistorico_retornaRegistrosDoContrato() {
        ContratoAlteracao entity = ContratoAlteracao.builder()
                .id(1L)
                .contratoId(5L)
                .username("teste")
                .changedAt(LocalDateTime.now())
                .changes(List.of(Map.of("path", "negocioValorTotal", "oldValue", "0", "newValue", "10")))
                .build();

        when(alteracaoRepository.findByContratoIdOrderByChangedAtDesc(5L))
                .thenReturn(List.of(entity));

        List<ContratoAlteracaoResponse> result = auditService.getHistorico(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContratoId()).isEqualTo(5L);
        assertThat(result.get(0).getUsername()).isEqualTo("teste");
        assertThat(result.get(0).getChanges()).hasSize(1);
        assertThat(result.get(0).getChanges()).hasSize(1);
        assertThat(result.get(0).getChanges().get(0).getPath()).isEqualTo("negocioValorTotal");
        assertThat(result.get(0).getChanges().get(0).getLabel()).isNotNull();
        assertThat(result.get(0).getChanges().get(0).getDisplayOld()).isEqualTo("0");
        assertThat(result.get(0).getChanges().get(0).getDisplayNew()).isEqualTo("10");
    }

    @Test
    void sanitizeAuditChanges_removeTecnicosEOldEqualsNew() {
        List<ContratoAlteracaoResponse.FieldChange> raw = List.of(
                ContratoAlteracaoResponse.FieldChange.builder().path("vendedores[0].id").oldValue("1").newValue("2").build(),
                ContratoAlteracaoResponse.FieldChange.builder().path("vendedores[0].nome").oldValue("Marlon").newValue("Marlon 7777").build(),
                ContratoAlteracaoResponse.FieldChange.builder().path("vendedores[0].ordem").oldValue("0").newValue("0").build(),
                ContratoAlteracaoResponse.FieldChange.builder().path("observacoes").oldValue("x").newValue("x").build()
        );
        List<ContratoAlteracaoResponse.FieldChange> out = auditService.sanitizeAuditChanges(raw);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getPath()).isEqualTo("vendedores[0].nome");
        assertThat(out.get(0).getOldValue()).isEqualTo("Marlon");
        assertThat(out.get(0).getNewValue()).isEqualTo("Marlon 7777");
    }

    @Test
    void labelForPath_vendedorNome_retornaVendedorNome() {
        assertThat(auditService.labelForPath("vendedores[0].nome")).isEqualTo("Vendedor - Nome");
        assertThat(auditService.labelForPath("observacoes")).isEqualTo("Observações");
    }

    @Test
    void formatValue_statusTraduzEnums() {
        assertThat(auditService.formatValue("status", "DRAFT")).isEqualTo("Rascunho");
        assertThat(auditService.formatValue("status", "FINAL")).isEqualTo("Finalizado");
        assertThat(auditService.formatValue("vendedores[0].nome", "João")).isEqualTo("João");
    }

    /** DTO mínimo para simular ContratoResponse com um campo. */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DummyResponse {
        private String negocioValorTotal;
    }
}

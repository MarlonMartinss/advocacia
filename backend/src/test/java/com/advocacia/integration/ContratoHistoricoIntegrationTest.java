package com.advocacia.integration;

import com.advocacia.dto.ContratoRequest;
import com.advocacia.dto.ContratoResponse;
import com.advocacia.repository.ContratoAlteracaoRepository;
import com.advocacia.repository.ContratoRepository;
import com.advocacia.service.ContratoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração: atualizar observacoes de um contrato deve inserir pelo menos 1 linha em contrato_alteracoes.
 * Requer banco (ex.: Postgres com perfil dev). Ignorar se não houver DB em ambiente de teste.
 */
@SpringBootTest
class ContratoHistoricoIntegrationTest {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ContratoAlteracaoRepository alteracaoRepository;

    @Test
    void update_observacoes_insere_linha_em_contrato_alteracoes() {
        ContratoRequest create = new ContratoRequest();
        create.setObservacoes("antes");
        ContratoResponse created = contratoService.create(create);
        Long contratoId = created.getId();
        assertThat(contratoId).isNotNull();

        long countBefore = alteracaoRepository.findByContratoIdOrderByChangedAtDesc(contratoId).size();

        ContratoRequest update = new ContratoRequest();
        update.setObservacoes("teste-historico-ok-1");
        contratoService.update(contratoId, update);

        long countAfter = alteracaoRepository.findByContratoIdOrderByChangedAtDesc(contratoId).size();
        assertThat(countAfter)
                .as("Deve existir pelo menos 1 registro de histórico após update de observacoes")
                .isGreaterThanOrEqualTo(1);
        assertThat(countAfter).isGreaterThan(countBefore);
    }
}

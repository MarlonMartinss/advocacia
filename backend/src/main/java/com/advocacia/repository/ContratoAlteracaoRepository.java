package com.advocacia.repository;

import com.advocacia.entity.ContratoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoAlteracaoRepository extends JpaRepository<ContratoAlteracao, Long> {
    List<ContratoAlteracao> findByContratoIdOrderByChangedAtDesc(Long contratoId);
}

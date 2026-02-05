package com.advocacia.repository;

import com.advocacia.entity.ContratoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoAnexoRepository extends JpaRepository<ContratoAnexo, Long> {

    List<ContratoAnexo> findByContratoIdOrderByCreatedAtDesc(Long contratoId);

    void deleteByContratoId(Long contratoId);
}

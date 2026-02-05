package com.advocacia.repository;

import com.advocacia.entity.Contrato;
import com.advocacia.entity.ContratoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByStatus(ContratoStatus status);

    List<Contrato> findAllByOrderByCreatedAtDesc();
}

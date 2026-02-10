package com.advocacia.dto;

import com.advocacia.entity.ContratoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoResponse {

    private Long id;
    private ContratoStatus status;
    private Integer paginaAtual;

    // ========== VENDEDORES (lista) ==========
    private List<VendedorResponse> vendedores;

    // ========== COMPRADORES (lista) ==========
    private List<CompradorResponse> compradores;

    // ========== PÁGINA 3: IMÓVEL OBJETO DO NEGÓCIO ==========
    private String imovelMatricula;
    private String imovelLivro;
    private String imovelOficio;
    private String imovelProprietario;
    private String imovelMomentoPosse;
    private String imovelPrazoTransferencia;
    private String imovelPrazoEscritura;
    private String imovelDescricao;

    // ========== PÁGINA 3: IMÓVEL DADO EM PERMUTA ==========
    private String permutaImovelMatricula;
    private String permutaImovelLivro;
    private String permutaImovelOficio;
    private String permutaImovelProprietario;
    private String permutaImovelMomentoPosse;
    private String permutaImovelPrazoTransferencia;
    private String permutaImovelPrazoEscritura;
    private String permutaImovelDescricao;

    // ========== PÁGINA 3: VEÍCULO DADO EM PERMUTA ==========
    private String veiculoMarca;
    private String veiculoAno;
    private String veiculoModelo;
    private String veiculoPlaca;
    private String veiculoChassi;
    private String veiculoCor;
    private String veiculoMotor;
    private String veiculoRenavam;
    private LocalDate veiculoDataEntrega;
    private Integer veiculoKm;

    // ========== PÁGINA 4: NEGÓCIO ==========
    private BigDecimal negocioValorTotal;
    private BigDecimal negocioValorEntrada;
    private String negocioFormaPagamento;
    private Integer negocioNumParcelas;
    private BigDecimal negocioValorParcela;
    private String negocioVencimentos;
    private BigDecimal negocioValorImovelPermuta;
    private BigDecimal negocioValorVeiculoPermuta;
    private BigDecimal negocioValorFinanciamento;
    private String negocioPrazoPagamento;
    private LocalDate negocioDataPrimeiraParcela;
    private List<ParcelaItemDto> parcelas;

    // ========== PÁGINA 4: CONTA BANCÁRIA ==========
    private String contaTitular;
    private String contaBanco;
    private String contaAgencia;
    private String contaPix;

    // ========== PÁGINA 4: HONORÁRIOS ==========
    private BigDecimal honorariosValor;
    private String honorariosFormaPagamento;
    private LocalDate honorariosDataPagamento;

    // ========== PÁGINA 4: OBSERVAÇÕES E ASSINATURAS ==========
    private String observacoes;
    private LocalDate dataContrato;
    private String assinaturaCorretor;
    private String assinaturaAgenciador;
    private String assinaturaGestor;

    // ========== AUDITORIA ==========
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

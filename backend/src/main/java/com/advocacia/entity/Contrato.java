package com.advocacia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contratos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== STATUS E CONTROLE ==========
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContratoStatus status = ContratoStatus.DRAFT;

    @Column(name = "pagina_atual")
    @Builder.Default
    private Integer paginaAtual = 1;

    // ========== PÁGINA 1: VENDEDOR PESSOA JURÍDICA ==========
    @Column(name = "vendedor_nome", length = 200)
    private String vendedorNome;

    @Column(name = "vendedor_cnpj", length = 20)
    private String vendedorCnpj;

    @Column(name = "vendedor_email", length = 150)
    private String vendedorEmail;

    @Column(name = "vendedor_telefone", length = 20)
    private String vendedorTelefone;

    @Column(name = "vendedor_endereco", length = 500)
    private String vendedorEndereco;

    // ========== PÁGINA 1: SÓCIO ADMINISTRADOR ==========
    @Column(name = "socio_nome", length = 200)
    private String socioNome;

    @Column(name = "socio_nacionalidade", length = 100)
    private String socioNacionalidade;

    @Column(name = "socio_profissao", length = 100)
    private String socioProfissao;

    @Column(name = "socio_estado_civil", length = 50)
    private String socioEstadoCivil;

    @Column(name = "socio_regime_bens", length = 100)
    private String socioRegimeBens;

    @Column(name = "socio_cpf", length = 14)
    private String socioCpf;

    @Column(name = "socio_rg", length = 20)
    private String socioRg;

    @Column(name = "socio_cnh", length = 20)
    private String socioCnh;

    @Column(name = "socio_email", length = 150)
    private String socioEmail;

    @Column(name = "socio_telefone", length = 20)
    private String socioTelefone;

    @Column(name = "socio_endereco", length = 500)
    private String socioEndereco;

    // ========== PÁGINA 2: COMPRADOR ==========
    @Column(name = "comprador_nome", length = 200)
    private String compradorNome;

    @Column(name = "comprador_nacionalidade", length = 100)
    private String compradorNacionalidade;

    @Column(name = "comprador_profissao", length = 100)
    private String compradorProfissao;

    @Column(name = "comprador_estado_civil", length = 50)
    private String compradorEstadoCivil;

    @Column(name = "comprador_regime_bens", length = 100)
    private String compradorRegimeBens;

    @Column(name = "comprador_cpf", length = 14)
    private String compradorCpf;

    @Column(name = "comprador_rg", length = 20)
    private String compradorRg;

    @Column(name = "comprador_cnh", length = 20)
    private String compradorCnh;

    @Column(name = "comprador_email", length = 150)
    private String compradorEmail;

    @Column(name = "comprador_telefone", length = 20)
    private String compradorTelefone;

    @Column(name = "comprador_endereco", length = 500)
    private String compradorEndereco;

    // ========== PÁGINA 2: CÔNJUGE/CONVIVENTE DO COMPRADOR ==========
    @Column(name = "conjuge_nome", length = 200)
    private String conjugeNome;

    @Column(name = "conjuge_nacionalidade", length = 100)
    private String conjugeNacionalidade;

    @Column(name = "conjuge_profissao", length = 100)
    private String conjugeProfissao;

    @Column(name = "conjuge_cpf", length = 14)
    private String conjugeCpf;

    @Column(name = "conjuge_rg", length = 20)
    private String conjugeRg;

    // ========== PÁGINA 3: IMÓVEL OBJETO DO NEGÓCIO ==========
    @Column(name = "imovel_matricula", length = 50)
    private String imovelMatricula;

    @Column(name = "imovel_livro", length = 50)
    private String imovelLivro;

    @Column(name = "imovel_oficio", length = 100)
    private String imovelOficio;

    @Column(name = "imovel_proprietario", length = 200)
    private String imovelProprietario;

    @Column(name = "imovel_momento_posse", length = 200)
    private String imovelMomentoPosse;

    @Column(name = "imovel_prazo_transferencia", length = 200)
    private String imovelPrazoTransferencia;

    @Column(name = "imovel_prazo_escritura", length = 200)
    private String imovelPrazoEscritura;

    @Column(name = "imovel_descricao", columnDefinition = "TEXT")
    private String imovelDescricao;

    // ========== PÁGINA 3: IMÓVEL DADO EM PERMUTA ==========
    @Column(name = "permuta_imovel_matricula", length = 50)
    private String permutaImovelMatricula;

    @Column(name = "permuta_imovel_livro", length = 50)
    private String permutaImovelLivro;

    @Column(name = "permuta_imovel_oficio", length = 100)
    private String permutaImovelOficio;

    @Column(name = "permuta_imovel_proprietario", length = 200)
    private String permutaImovelProprietario;

    @Column(name = "permuta_imovel_momento_posse", length = 200)
    private String permutaImovelMomentoPosse;

    @Column(name = "permuta_imovel_prazo_transferencia", length = 200)
    private String permutaImovelPrazoTransferencia;

    @Column(name = "permuta_imovel_prazo_escritura", length = 200)
    private String permutaImovelPrazoEscritura;

    @Column(name = "permuta_imovel_descricao", columnDefinition = "TEXT")
    private String permutaImovelDescricao;

    // ========== PÁGINA 3: VEÍCULO DADO EM PERMUTA ==========
    @Column(name = "veiculo_marca", length = 100)
    private String veiculoMarca;

    @Column(name = "veiculo_ano", length = 10)
    private String veiculoAno;

    @Column(name = "veiculo_modelo", length = 100)
    private String veiculoModelo;

    @Column(name = "veiculo_placa", length = 10)
    private String veiculoPlaca;

    @Column(name = "veiculo_chassi", length = 50)
    private String veiculoChassi;

    @Column(name = "veiculo_cor", length = 50)
    private String veiculoCor;

    @Column(name = "veiculo_motor", length = 50)
    private String veiculoMotor;

    @Column(name = "veiculo_renavam", length = 20)
    private String veiculoRenavam;

    @Column(name = "veiculo_data_entrega")
    private LocalDate veiculoDataEntrega;

    @Column(name = "veiculo_km")
    private Integer veiculoKm;

    // ========== PÁGINA 4: NEGÓCIO ==========
    @Column(name = "negocio_valor_total", precision = 15, scale = 2)
    private BigDecimal negocioValorTotal;

    @Column(name = "negocio_valor_entrada", precision = 15, scale = 2)
    private BigDecimal negocioValorEntrada;

    @Column(name = "negocio_forma_pagamento", length = 500)
    private String negocioFormaPagamento;

    @Column(name = "negocio_num_parcelas")
    private Integer negocioNumParcelas;

    @Column(name = "negocio_valor_parcela", precision = 15, scale = 2)
    private BigDecimal negocioValorParcela;

    @Column(name = "negocio_vencimentos", length = 500)
    private String negocioVencimentos;

    @Column(name = "negocio_valor_imovel_permuta", precision = 15, scale = 2)
    private BigDecimal negocioValorImovelPermuta;

    @Column(name = "negocio_valor_veiculo_permuta", precision = 15, scale = 2)
    private BigDecimal negocioValorVeiculoPermuta;

    @Column(name = "negocio_valor_financiamento", precision = 15, scale = 2)
    private BigDecimal negocioValorFinanciamento;

    @Column(name = "negocio_prazo_pagamento", length = 200)
    private String negocioPrazoPagamento;

    @Column(name = "negocio_data_primeira_parcela")
    private LocalDate negocioDataPrimeiraParcela;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "negocio_parcelas", columnDefinition = "jsonb")
    private String negocioParcelas;

    // ========== PÁGINA 4: CONTA BANCÁRIA ==========
    @Column(name = "conta_titular", length = 200)
    private String contaTitular;

    @Column(name = "conta_banco", length = 100)
    private String contaBanco;

    @Column(name = "conta_agencia", length = 20)
    private String contaAgencia;

    @Column(name = "conta_pix", length = 200)
    private String contaPix;

    // ========== PÁGINA 4: HONORÁRIOS ==========
    @Column(name = "honorarios_valor", precision = 15, scale = 2)
    private BigDecimal honorariosValor;

    @Column(name = "honorarios_forma_pagamento", length = 200)
    private String honorariosFormaPagamento;

    @Column(name = "honorarios_data_pagamento")
    private LocalDate honorariosDataPagamento;

    // ========== PÁGINA 4: OBSERVAÇÕES E ASSINATURAS ==========
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_contrato")
    private LocalDate dataContrato;

    @Column(name = "assinatura_corretor", length = 200)
    private String assinaturaCorretor;

    @Column(name = "assinatura_agenciador", length = 200)
    private String assinaturaAgenciador;

    @Column(name = "assinatura_gestor", length = 200)
    private String assinaturaGestor;

    // ========== VENDEDORES E COMPRADORES (N:1) ==========
    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @org.hibernate.annotations.BatchSize(size = 20)
    @Builder.Default
    private List<ContratoVendedor> vendedores = new ArrayList<>();

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @org.hibernate.annotations.BatchSize(size = 20)
    @Builder.Default
    private List<ContratoComprador> compradores = new ArrayList<>();

    // ========== AUDITORIA ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

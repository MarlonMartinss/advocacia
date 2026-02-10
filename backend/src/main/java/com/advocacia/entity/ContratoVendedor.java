package com.advocacia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contrato_vendedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoVendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;

    // Dados do vendedor
    @Column(length = 200)
    private String nome;

    @Column(length = 20)
    private String documento;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 500)
    private String endereco;

    // Socio Administrador (somente quando documento eh CNPJ)
    @Column(name = "socio_nome", length = 200)
    private String socioNome;

    @Column(name = "socio_cpf", length = 14)
    private String socioCpf;

    @Column(name = "socio_nacionalidade", length = 100)
    private String socioNacionalidade;

    @Column(name = "socio_profissao", length = 100)
    private String socioProfissao;

    @Column(name = "socio_estado_civil", length = 50)
    private String socioEstadoCivil;

    @Column(name = "socio_regime_bens", length = 100)
    private String socioRegimeBens;

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
}

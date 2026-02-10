package com.advocacia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contrato_compradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoComprador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;

    // Dados do comprador
    @Column(length = 200)
    private String nome;

    @Column(length = 20)
    private String documento;

    @Column(length = 100)
    private String nacionalidade;

    @Column(length = 100)
    private String profissao;

    @Column(name = "estado_civil", length = 50)
    private String estadoCivil;

    @Column(name = "regime_bens", length = 100)
    private String regimeBens;

    @Column(length = 20)
    private String rg;

    @Column(length = 20)
    private String cnh;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 500)
    private String endereco;

    // Conjuge (somente quando estado_civil = Casado ou Uniao Estavel)
    @Column(name = "conjuge_nome", length = 200)
    private String conjugeNome;

    @Column(name = "conjuge_cpf", length = 14)
    private String conjugeCpf;

    @Column(name = "conjuge_nacionalidade", length = 100)
    private String conjugeNacionalidade;

    @Column(name = "conjuge_profissao", length = 100)
    private String conjugeProfissao;

    @Column(name = "conjuge_rg", length = 20)
    private String conjugeRg;
}

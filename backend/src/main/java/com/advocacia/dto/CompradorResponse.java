package com.advocacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompradorResponse {
    private Long id;
    private Integer ordem;

    private String nome;
    private String documento;
    private String nacionalidade;
    private String profissao;
    private String estadoCivil;
    private String regimeBens;
    private String rg;
    private String cnh;
    private String email;
    private String telefone;
    private String endereco;

    // Conjuge
    private String conjugeNome;
    private String conjugeCpf;
    private String conjugeNacionalidade;
    private String conjugeProfissao;
    private String conjugeRg;
}

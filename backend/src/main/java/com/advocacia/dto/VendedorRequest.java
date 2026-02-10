package com.advocacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendedorRequest {
    private Long id;
    private Integer ordem;

    private String nome;
    private String documento;
    private String email;
    private String telefone;
    private String endereco;

    // Socio Administrador
    private String socioNome;
    private String socioCpf;
    private String socioNacionalidade;
    private String socioProfissao;
    private String socioEstadoCivil;
    private String socioRegimeBens;
    private String socioRg;
    private String socioCnh;
    private String socioEmail;
    private String socioTelefone;
    private String socioEndereco;
}

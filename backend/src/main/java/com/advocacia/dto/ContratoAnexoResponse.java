package com.advocacia.dto;

import com.advocacia.entity.ContratoAnexo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoAnexoResponse {

    private Long id;
    private Long contratoId;
    private String nomeOriginal;
    private String nomeArquivo;
    private String tipoMime;
    private Long tamanho;
    private LocalDateTime createdAt;

    public static ContratoAnexoResponse fromEntity(ContratoAnexo anexo) {
        return ContratoAnexoResponse.builder()
                .id(anexo.getId())
                .contratoId(anexo.getContrato().getId())
                .nomeOriginal(anexo.getNomeOriginal())
                .nomeArquivo(anexo.getNomeArquivo())
                .tipoMime(anexo.getTipoMime())
                .tamanho(anexo.getTamanho())
                .createdAt(anexo.getCreatedAt())
                .build();
    }
}

package com.advocacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoAlteracaoResponse {
    private Long id;
    private Long contratoId;
    private String username;
    private LocalDateTime changedAt;
    private List<FieldChange> changes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldChange {
        private String path;
        private String oldValue;
        private String newValue;
        /** Rótulo amigável para exibição (ex.: "Vendedor - Nome"). */
        private String label;
        /** Valor antigo formatado para exibição (ex.: "Rascunho" em vez de "DRAFT"). */
        private String displayOld;
        /** Valor novo formatado para exibição. */
        private String displayNew;
    }
}

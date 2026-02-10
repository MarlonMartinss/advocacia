package com.advocacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelaItemDto {
    private Integer numero;
    /** Data de vencimento em ISO (yyyy-MM-dd). */
    private String vencimento;
    private BigDecimal valor;
}

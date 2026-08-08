package com.supplychainmultiwarehousetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferItemResponseDto {
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private Integer requestedQuantity;
    private Integer transferredQuantity;
}

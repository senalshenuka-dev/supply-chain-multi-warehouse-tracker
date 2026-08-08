package com.supplychainmultiwarehousetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDto {
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String productSku;
    private String productBarcode;
    private String productName;
    private String productDescription;
    private BigDecimal unitPrice;
    private Integer minThreshold;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private boolean lowStock;
    private LocalDateTime lastUpdated;
}

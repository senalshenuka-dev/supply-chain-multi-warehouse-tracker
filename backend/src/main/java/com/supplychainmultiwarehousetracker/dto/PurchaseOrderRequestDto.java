package com.supplychainmultiwarehousetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderRequestDto {
    private Long vendorId;
    private Long warehouseId;
    private String notes;
    private String expectedDeliveryDate;
    private List<POItemDto> items;

    @Data
    public static class POItemDto {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}

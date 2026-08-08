package com.supplychainmultiwarehousetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesOrderIngestDto {
    private String channelSource;
    private String customerName;
    private String customerEmail;
    private Long warehouseId;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Long productId;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}

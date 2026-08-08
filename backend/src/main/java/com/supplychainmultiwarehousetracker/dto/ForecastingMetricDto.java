package com.supplychainmultiwarehousetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastingMetricDto {
    private Long productId;
    private String productSku;
    private String productName;
    private BigDecimal avgDailyVelocity;
    private Integer leadTimeDays;
    private Integer calculatedReorderPoint;
    private Integer currentStock;
    private String reorderStatus;
}

package com.supplychainmultiwarehousetracker.dto;

import lombok.Data;

@Data
public class BarcodeScanRequestDto {
    private String barcode;
    private Long warehouseId;
    private Integer adjustmentQuantity;
    private String action;
}

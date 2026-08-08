package com.supplychainmultiwarehousetracker.dto;

import com.supplychainmultiwarehousetracker.domain.model.TransferStatus;
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
public class TransferResponseDto {
    private Long id;
    private String transferNumber;
    private Long sourceWarehouseId;
    private String sourceWarehouseCode;
    private String sourceWarehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseCode;
    private String targetWarehouseName;
    private TransferStatus status;
    private String initiatedByUsername;
    private String dispatchedByUsername;
    private String receivedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferItemResponseDto> items;
}

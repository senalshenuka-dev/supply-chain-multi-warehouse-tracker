package com.supplychainmultiwarehousetracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDto {
    @NotNull(message = "Source warehouse ID is required")
    private Long sourceWarehouseId;

    @NotNull(message = "Target warehouse ID is required")
    private Long targetWarehouseId;

    @NotEmpty(message = "At least one item must be included in the transfer")
    @Valid
    private List<TransferItemRequestDto> items;
}

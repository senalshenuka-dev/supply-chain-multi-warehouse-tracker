package com.supplychainmultiwarehousetracker.dto;

import com.supplychainmultiwarehousetracker.domain.model.TransferStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferStatusUpdateDto {
    @NotNull(message = "Target status is required")
    private TransferStatus targetStatus;
}

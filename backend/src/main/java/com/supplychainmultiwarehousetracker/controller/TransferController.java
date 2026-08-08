package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.domain.model.TransferStatus;
import com.supplychainmultiwarehousetracker.dto.*;
import com.supplychainmultiwarehousetracker.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final StockTransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransfer(
            @Valid @RequestBody TransferRequestDto request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        TransferResponseDto response = transferService.initiateTransfer(request, username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TransferResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransferStatusUpdateDto request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        TransferResponseDto response = transferService.updateTransferStatus(id, request.getTargetStatus(), username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<TransferResponseDto>> getTransfers(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponseDto<TransferResponseDto> response = transferService.getTransfersPaginated(status, warehouseId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDto> getTransferById(@PathVariable Long id) {
        TransferResponseDto response = transferService.getTransferById(id);
        return ResponseEntity.ok(response);
    }
}

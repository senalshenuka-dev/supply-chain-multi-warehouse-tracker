package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.dto.InventoryResponseDto;
import com.supplychainmultiwarehousetracker.dto.LowStockAlertDto;
import com.supplychainmultiwarehousetracker.dto.PageResponseDto;
import com.supplychainmultiwarehousetracker.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/inventory")
    public ResponseEntity<PageResponseDto<InventoryResponseDto>> getInventory(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false, name = "sku") String skuOrQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "product.sku") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        PageResponseDto<InventoryResponseDto> response = inventoryService.getInventoryPaginated(warehouseId, skuOrQuery, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<LowStockAlertDto>> getLowStockAlerts() {
        List<LowStockAlertDto> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/inventory/scan")
    public ResponseEntity<InventoryResponseDto> scanBarcode(
            @RequestBody com.supplychainmultiwarehousetracker.dto.BarcodeScanRequestDto request,
            java.security.Principal principal
    ) {
        String username = principal != null ? principal.getName() : "system";
        InventoryResponseDto response = inventoryService.processBarcodeScan(request, username);
        return ResponseEntity.ok(response);
    }
}

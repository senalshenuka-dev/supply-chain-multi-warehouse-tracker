package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.Product;
import com.supplychainmultiwarehousetracker.domain.model.WarehouseInventory;
import com.supplychainmultiwarehousetracker.domain.repository.ProductRepository;
import com.supplychainmultiwarehousetracker.domain.repository.WarehouseInventoryRepository;
import com.supplychainmultiwarehousetracker.dto.BarcodeScanRequestDto;
import com.supplychainmultiwarehousetracker.dto.InventoryResponseDto;
import com.supplychainmultiwarehousetracker.dto.LowStockAlertDto;
import com.supplychainmultiwarehousetracker.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final WarehouseInventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponseDto> getInventoryPaginated(Long warehouseId, String query, Pageable pageable) {
        Page<WarehouseInventory> page = inventoryRepository.findInventoryFiltered(warehouseId, query, pageable);
        Page<InventoryResponseDto> dtoPage = page.map(this::mapToDto);
        return PageResponseDto.fromPage(dtoPage);
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertDto> getLowStockAlerts() {
        List<WarehouseInventory> lowStockList = inventoryRepository.findLowStockInventory();
        return lowStockList.stream().map(inv -> LowStockAlertDto.builder()
                .warehouseId(inv.getWarehouse().getId())
                .warehouseCode(inv.getWarehouse().getCode())
                .warehouseName(inv.getWarehouse().getName())
                .productId(inv.getProduct().getId())
                .productSku(inv.getProduct().getSku())
                .productName(inv.getProduct().getName())
                .currentQuantity(inv.getQuantity())
                .minThreshold(inv.getProduct().getMinThreshold())
                .deficit(inv.getProduct().getMinThreshold() - inv.getQuantity())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponseDto processBarcodeScan(BarcodeScanRequestDto request, String username) {
        Product product = productRepository.findBySkuOrBarcode(request.getBarcode(), request.getBarcode())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with Barcode/SKU: " + request.getBarcode()));

        Long warehouseId = request.getWarehouseId() != null ? request.getWarehouseId() : 1L;
        WarehouseInventory inventory = inventoryRepository.findByWarehouseIdAndProductId(warehouseId, product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory record not found for warehouse " + warehouseId + " and product " + product.getSku()));

        int qtyChange = request.getAdjustmentQuantity() != null ? request.getAdjustmentQuantity() : 1;
        if ("DECREMENT".equalsIgnoreCase(request.getAction())) {
            qtyChange = -qtyChange;
        }

        int newQty = Math.max(0, inventory.getQuantity() + qtyChange);
        inventory.setQuantity(newQty);
        WarehouseInventory updated = inventoryRepository.save(inventory);

        auditLogService.log("Product", product.getId().toString(), "BARCODE_SCAN_ADJUSTMENT", username,
                "Adjusted inventory for " + product.getSku() + " in Warehouse " + warehouseId + " by " + qtyChange + ". New Qty: " + newQty);

        return mapToDto(updated);
    }

    public InventoryResponseDto mapToDto(WarehouseInventory inv) {
        return InventoryResponseDto.builder()
                .warehouseId(inv.getWarehouse().getId())
                .warehouseCode(inv.getWarehouse().getCode())
                .warehouseName(inv.getWarehouse().getName())
                .productId(inv.getProduct().getId())
                .productSku(inv.getProduct().getSku())
                .productBarcode(inv.getProduct().getBarcode())
                .productName(inv.getProduct().getName())
                .productDescription(inv.getProduct().getDescription())
                .unitPrice(inv.getProduct().getUnitPrice())
                .minThreshold(inv.getProduct().getMinThreshold())
                .quantity(inv.getQuantity())
                .reservedQuantity(inv.getReservedQuantity())
                .availableQuantity(inv.getAvailableQuantity())
                .lowStock(inv.isLowStock())
                .lastUpdated(inv.getLastUpdated())
                .build();
    }
}

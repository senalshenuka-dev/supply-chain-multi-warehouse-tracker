package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.*;
import com.supplychainmultiwarehousetracker.domain.repository.*;
import com.supplychainmultiwarehousetracker.dto.PurchaseOrderRequestDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getVendorPurchaseOrders(String vendorEmailOrCode) {
        Vendor vendor = vendorRepository.findByContactEmail(vendorEmailOrCode)
                .or(() -> vendorRepository.findByCode(vendorEmailOrCode))
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found for " + vendorEmailOrCode));
        return purchaseOrderRepository.findByVendorId(vendor.getId());
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequestDto request, String username) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + request.getVendorId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + request.getWarehouseId()));

        String poNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNumber)
                .vendor(vendor)
                .warehouse(warehouse)
                .status("PENDING_APPROVAL")
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (PurchaseOrderRequestDto.POItemDto itemDto : request.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemDto.getProductId()));
                BigDecimal lineTotal = itemDto.getUnitPrice().multiply(new BigDecimal(itemDto.getQuantity()));
                total = total.add(lineTotal);

                PurchaseOrderItem poi = PurchaseOrderItem.builder()
                        .purchaseOrder(po)
                        .product(product)
                        .requestedQuantity(itemDto.getQuantity())
                        .receivedQuantity(0)
                        .unitPrice(itemDto.getUnitPrice())
                        .build();
                items.add(poi);
            }
        }

        po.setTotalAmount(total);
        po.setItems(items);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.log("PurchaseOrder", saved.getId().toString(), "PO_CREATE", username,
                "Created Purchase Order " + poNumber + " for Vendor " + vendor.getName() + " with Total $" + total);

        return saved;
    }

    @Transactional
    public List<PurchaseOrder> autoGeneratePOsFromLowStock(String username) {
        List<WarehouseInventory> lowStockItems = inventoryRepository.findLowStockInventory();
        if (lowStockItems.isEmpty()) {
            return List.of();
        }

        Vendor defaultVendor = vendorRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No default vendor configured for auto-PO generation"));

        List<PurchaseOrder> createdPOs = new ArrayList<>();
        // Group by warehouse
        for (WarehouseInventory inv : lowStockItems) {
            int deficit = Math.max(10, inv.getProduct().getMinThreshold() * 2 - inv.getQuantity());
            PurchaseOrderRequestDto dto = new PurchaseOrderRequestDto();
            dto.setVendorId(defaultVendor.getId());
            dto.setWarehouseId(inv.getWarehouse().getId());
            dto.setNotes("Auto-generated PO triggered by low-stock threshold alert for " + inv.getProduct().getSku());

            PurchaseOrderRequestDto.POItemDto itemDto = new PurchaseOrderRequestDto.POItemDto();
            itemDto.setProductId(inv.getProduct().getId());
            itemDto.setQuantity(deficit);
            itemDto.setUnitPrice(inv.getProduct().getUnitPrice());
            dto.setItems(List.of(itemDto));

            PurchaseOrder po = createPurchaseOrder(dto, username);
            createdPOs.add(po);
        }

        return createdPOs;
    }

    @Transactional
    public PurchaseOrder approvePurchaseOrder(Long id, String username) {
        PurchaseOrder po = getPurchaseOrderById(id);
        po.setStatus("APPROVED");
        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogService.log("PurchaseOrder", po.getId().toString(), "PO_APPROVE", username,
                "Approved Purchase Order " + po.getPoNumber());
        return updated;
    }

    @Transactional
    public PurchaseOrder updatePOStatusBySupplier(Long id, String status, String etaDateStr, String dispatchDocUrl, String username) {
        PurchaseOrder po = getPurchaseOrderById(id);
        if ("SENT".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status)) {
            po.setStatus("SENT");
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            po.setStatus("REJECTED");
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            po.setStatus("COMPLETED");
            // Receive inventory
            for (PurchaseOrderItem item : po.getItems()) {
                WarehouseInventory inv = inventoryRepository.findByWarehouseIdAndProductId(po.getWarehouse().getId(), item.getProduct().getId())
                        .orElseGet(() -> WarehouseInventory.builder()
                                .warehouse(po.getWarehouse())
                                .product(item.getProduct())
                                .quantity(0)
                                .reservedQuantity(0)
                                .build());
                item.setReceivedQuantity(item.getRequestedQuantity());
                inv.setQuantity(inv.getQuantity() + item.getRequestedQuantity());
                inventoryRepository.save(inv);
            }
        }

        if (dispatchDocUrl != null && !dispatchDocUrl.isBlank()) {
            po.setDispatchDocUrl(dispatchDocUrl);
        }

        PurchaseOrder updated = purchaseOrderRepository.save(po);
        auditLogService.log("PurchaseOrder", po.getId().toString(), "SUPPLIER_PO_UPDATE", username,
                "Supplier updated PO " + po.getPoNumber() + " status to " + status);
        return updated;
    }
}

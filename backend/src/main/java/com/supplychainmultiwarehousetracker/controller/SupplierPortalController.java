package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.domain.model.PurchaseOrder;
import com.supplychainmultiwarehousetracker.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/supplier-portal")
@RequiredArgsConstructor
public class SupplierPortalController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseOrder>> getAssignedPOs(Principal principal) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getVendorPurchaseOrders("orders@siliconmicro.com"));
        } catch (Exception e) {
            return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
        }
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<PurchaseOrder> updatePOStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "supplier1";
        String status = payload.getOrDefault("status", "SENT");
        String etaDate = payload.get("etaDate");
        String dispatchDocUrl = payload.get("dispatchDocUrl");
        return ResponseEntity.ok(purchaseOrderService.updatePOStatusBySupplier(id, status, etaDate, dispatchDocUrl, username));
    }
}

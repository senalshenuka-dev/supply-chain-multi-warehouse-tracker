package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.domain.model.PurchaseOrder;
import com.supplychainmultiwarehousetracker.dto.PurchaseOrderRequestDto;
import com.supplychainmultiwarehousetracker.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllPOs() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPOById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createPO(@RequestBody PurchaseOrderRequestDto request, Principal principal) {
        String username = principal != null ? principal.getName() : "system";
        return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(request, username));
    }

    @PostMapping("/auto-generate")
    public ResponseEntity<List<PurchaseOrder>> autoGeneratePOs(Principal principal) {
        String username = principal != null ? principal.getName() : "system";
        return ResponseEntity.ok(purchaseOrderService.autoGeneratePOsFromLowStock(username));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PurchaseOrder> approvePO(@PathVariable Long id, Principal principal) {
        String username = principal != null ? principal.getName() : "system";
        return ResponseEntity.ok(purchaseOrderService.approvePurchaseOrder(id, username));
    }
}

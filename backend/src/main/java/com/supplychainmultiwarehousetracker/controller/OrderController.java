package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.domain.model.SalesOrder;
import com.supplychainmultiwarehousetracker.dto.SalesOrderIngestDto;
import com.supplychainmultiwarehousetracker.service.OrderSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderSyncService orderSyncService;

    @GetMapping
    public ResponseEntity<List<SalesOrder>> getAllOrders() {
        return ResponseEntity.ok(orderSyncService.getAllSalesOrders());
    }

    @PostMapping("/ingest")
    public ResponseEntity<SalesOrder> ingestOrder(@RequestBody SalesOrderIngestDto dto, Principal principal) {
        String username = principal != null ? principal.getName() : "channel-api";
        return ResponseEntity.ok(orderSyncService.ingestSalesOrder(dto, username));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SalesOrder> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "warehouse1";
        String status = payload.getOrDefault("status", "PICKED");
        return ResponseEntity.ok(orderSyncService.updateOrderStatus(id, status, username));
    }
}

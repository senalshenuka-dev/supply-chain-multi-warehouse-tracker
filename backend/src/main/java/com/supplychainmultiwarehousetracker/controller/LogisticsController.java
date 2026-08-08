package com.supplychainmultiwarehousetracker.controller;

import com.supplychainmultiwarehousetracker.domain.model.ShippingManifest;
import com.supplychainmultiwarehousetracker.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping("/rates")
    public ResponseEntity<List<Map<String, Object>>> getShippingRates(
            @RequestParam Long salesOrderId,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) Double weightKg
    ) {
        return ResponseEntity.ok(logisticsService.calculateShippingRates(salesOrderId, zip, weightKg));
    }

    @PostMapping("/ship")
    public ResponseEntity<ShippingManifest> generateShippingManifest(
            @RequestBody Map<String, Object> payload,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "warehouse1";
        Long salesOrderId = Long.valueOf(payload.get("salesOrderId").toString());
        String carrier = payload.getOrDefault("carrier", "FedEx Express").toString();
        BigDecimal cost = payload.get("cost") != null ? new BigDecimal(payload.get("cost").toString()) : new BigDecimal("12.50");

        return ResponseEntity.ok(logisticsService.createShippingManifest(salesOrderId, carrier, cost, username));
    }

    @GetMapping("/manifest/{orderId}")
    public ResponseEntity<ShippingManifest> getManifest(@PathVariable Long orderId) {
        return ResponseEntity.ok(logisticsService.getManifestByOrderId(orderId));
    }
}

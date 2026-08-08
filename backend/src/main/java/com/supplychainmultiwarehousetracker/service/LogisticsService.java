package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.SalesOrder;
import com.supplychainmultiwarehousetracker.domain.model.ShippingManifest;
import com.supplychainmultiwarehousetracker.domain.repository.SalesOrderRepository;
import com.supplychainmultiwarehousetracker.domain.repository.ShippingManifestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final ShippingManifestRepository shippingManifestRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderSyncService orderSyncService;
    private final AuditLogService auditLogService;

    public List<Map<String, Object>> calculateShippingRates(Long salesOrderId, String zip, Double weightKg) {
        double baseWeight = weightKg != null ? weightKg : 2.5;

        List<Map<String, Object>> rates = new ArrayList<>();
        rates.add(Map.of("carrier", "FedEx Express", "service", "Overnight", "cost", new BigDecimal(18.50 + baseWeight * 2.20).setScale(2, BigDecimal.ROUND_HALF_UP), "estimatedDays", 1));
        rates.add(Map.of("carrier", "UPS Ground", "service", "Standard Ground", "cost", new BigDecimal(9.99 + baseWeight * 1.10).setScale(2, BigDecimal.ROUND_HALF_UP), "estimatedDays", 3));
        rates.add(Map.of("carrier", "DHL Express", "service", "Worldwide", "cost", new BigDecimal(24.00 + baseWeight * 3.00).setScale(2, BigDecimal.ROUND_HALF_UP), "estimatedDays", 2));
        rates.add(Map.of("carrier", "USPS Priority", "service", "Priority Mail", "cost", new BigDecimal(7.95 + baseWeight * 0.85).setScale(2, BigDecimal.ROUND_HALF_UP), "estimatedDays", 4));

        return rates;
    }

    @Transactional
    public ShippingManifest createShippingManifest(Long salesOrderId, String carrier, BigDecimal cost, String username) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales Order not found: " + salesOrderId));

        String trackingNumber = carrier.substring(0, 3).toUpperCase() + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String labelPdfUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + trackingNumber;

        ShippingManifest manifest = ShippingManifest.builder()
                .salesOrder(order)
                .carrier(carrier)
                .trackingNumber(trackingNumber)
                .shippingCost(cost != null ? cost : new BigDecimal("12.50"))
                .labelPdfUrl(labelPdfUrl)
                .build();

        ShippingManifest saved = shippingManifestRepository.save(manifest);

        // Update sales order status to SHIPPED
        orderSyncService.updateOrderStatus(salesOrderId, "SHIPPED", username);

        auditLogService.log("ShippingManifest", saved.getId().toString(), "GENERATE_SHIPPING_LABEL", username,
                "Generated shipping label for Order " + order.getOrderNumber() + " via " + carrier + ". Tracking #: " + trackingNumber);

        return saved;
    }

    @Transactional(readOnly = true)
    public ShippingManifest getManifestByOrderId(Long salesOrderId) {
        return shippingManifestRepository.findBySalesOrderId(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping Manifest not found for order: " + salesOrderId));
    }
}

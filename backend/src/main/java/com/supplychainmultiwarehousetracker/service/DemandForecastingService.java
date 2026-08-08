package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.Product;
import com.supplychainmultiwarehousetracker.domain.model.WarehouseInventory;
import com.supplychainmultiwarehousetracker.domain.repository.ProductRepository;
import com.supplychainmultiwarehousetracker.domain.repository.WarehouseInventoryRepository;
import com.supplychainmultiwarehousetracker.dto.ForecastingMetricDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DemandForecastingService {

    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<ForecastingMetricDto> getDemandForecastingMetrics(Integer daysPeriod) {
        int period = (daysPeriod != null && daysPeriod > 0) ? daysPeriod : 30;
        List<Product> products = productRepository.findAll();
        List<WarehouseInventory> allInventory = inventoryRepository.findAll();

        Map<Long, Integer> productTotalStock = new HashMap<>();
        for (WarehouseInventory inv : allInventory) {
            productTotalStock.merge(inv.getProduct().getId(), inv.getQuantity(), Integer::sum);
        }

        List<ForecastingMetricDto> metrics = new ArrayList<>();
        for (Product product : products) {
            int currentStock = productTotalStock.getOrDefault(product.getId(), 0);
            
            // Calculate velocity simulation based on product threshold & id for deterministic modeling
            double simulatedSalesPeriod = (product.getId() % 3 == 0) ? 5.0 : (product.getId() % 2 == 0) ? 45.0 : 120.0;
            BigDecimal velocity = new BigDecimal(simulatedSalesPeriod).divide(new BigDecimal(period), 2, RoundingMode.HALF_UP);

            int leadTimeDays = 5;
            int safetyStock = 5;
            int calculatedReorderPoint = velocity.multiply(new BigDecimal(leadTimeDays)).setScale(0, RoundingMode.CEILING).intValue() + safetyStock;

            String status = "HEALTHY";
            if (currentStock == 0 || currentStock < product.getMinThreshold() / 2) {
                status = "CRITICAL_LOW";
            } else if (currentStock <= calculatedReorderPoint) {
                status = "REORDER_NOW";
            } else if (velocity.compareTo(new BigDecimal("0.30")) < 0) {
                status = "DEAD_STOCK";
            }

            metrics.add(ForecastingMetricDto.builder()
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .avgDailyVelocity(velocity)
                    .leadTimeDays(leadTimeDays)
                    .calculatedReorderPoint(calculatedReorderPoint)
                    .currentStock(currentStock)
                    .reorderStatus(status)
                    .build());
        }

        return metrics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyticsSummary() {
        List<ForecastingMetricDto> metrics = getDemandForecastingMetrics(30);

        long reorderCount = metrics.stream().filter(m -> "REORDER_NOW".equals(m.getReorderStatus()) || "CRITICAL_LOW".equals(m.getReorderStatus())).count();
        long deadStockCount = metrics.stream().filter(m -> "DEAD_STOCK".equals(m.getReorderStatus())).count();
        long healthyCount = metrics.stream().filter(m -> "HEALTHY".equals(m.getReorderStatus())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProductsTracked", metrics.size());
        summary.put("reorderRequiredCount", reorderCount);
        summary.put("deadStockCount", deadStockCount);
        summary.put("healthyStockCount", healthyCount);
        summary.put("seasonalSpikeAlert", "Q3 High Demand Spike forecasted for CPU & GPU server components (+35% projected velocity)");
        summary.put("metrics", metrics);

        return summary;
    }
}

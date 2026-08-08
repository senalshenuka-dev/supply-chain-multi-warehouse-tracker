package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.*;
import com.supplychainmultiwarehousetracker.domain.repository.*;
import com.supplychainmultiwarehousetracker.dto.SalesOrderIngestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderSyncService {

    private final SalesOrderRepository salesOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    @Transactional
    public SalesOrder ingestSalesOrder(SalesOrderIngestDto dto, String username) {
        Long warehouseId = dto.getWarehouseId() != null ? dto.getWarehouseId() : 1L;
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + warehouseId));

        String orderNum = "ORD-" + dto.getChannelSource().toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        SalesOrder order = SalesOrder.builder()
                .orderNumber(orderNum)
                .channelSource(dto.getChannelSource())
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .warehouse(warehouse)
                .status("PENDING")
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<SalesOrderItem> items = new ArrayList<>();

        if (dto.getItems() != null) {
            for (SalesOrderIngestDto.OrderItemDto itemDto : dto.getItems()) {
                Product product = null;
                if (itemDto.getProductId() != null) {
                    product = productRepository.findById(itemDto.getProductId()).orElse(null);
                }
                if (product == null && itemDto.getSku() != null) {
                    product = productRepository.findBySku(itemDto.getSku()).orElse(null);
                }
                if (product == null) {
                    product = productRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No product available for order"));
                }

                final Product targetProduct = product;
                // Transactional Atomic Stock Reservation check
                WarehouseInventory inventory = inventoryRepository.findByWarehouseIdAndProductId(warehouse.getId(), targetProduct.getId())
                        .orElseThrow(() -> new IllegalArgumentException("No inventory record for product " + targetProduct.getSku() + " in warehouse " + warehouse.getId()));

                if (inventory.getAvailableQuantity() < itemDto.getQuantity()) {
                    throw new IllegalStateException("Insufficient available inventory for product " + targetProduct.getSku() + ". Requested: " + itemDto.getQuantity() + ", Available: " + inventory.getAvailableQuantity());
                }

                // Reserve quantity atomically
                inventory.setReservedQuantity(inventory.getReservedQuantity() + itemDto.getQuantity());
                inventoryRepository.save(inventory);

                BigDecimal unitPrice = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : product.getUnitPrice();
                BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(itemDto.getQuantity()));
                total = total.add(lineTotal);

                SalesOrderItem soi = SalesOrderItem.builder()
                        .salesOrder(order)
                        .product(product)
                        .quantity(itemDto.getQuantity())
                        .unitPrice(unitPrice)
                        .build();
                items.add(soi);
            }
        }

        order.setTotalAmount(total);
        order.setItems(items);
        order.setStatus("ALLOCATED");

        SalesOrder saved = salesOrderRepository.save(order);

        auditLogService.log("SalesOrder", saved.getId().toString(), "ORDER_INGEST", username,
                "Ingested order " + orderNum + " from " + dto.getChannelSource() + " with Total $" + total + ". Reserved inventory allocated.");

        return saved;
    }

    @Transactional
    public SalesOrder updateOrderStatus(Long orderId, String newStatus, String username) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        String prevStatus = order.getStatus();
        order.setStatus(newStatus.toUpperCase());

        if ("SHIPPED".equalsIgnoreCase(newStatus) && !"SHIPPED".equalsIgnoreCase(prevStatus)) {
            // Deduct reserved and total quantity
            for (SalesOrderItem item : order.getItems()) {
                WarehouseInventory inv = inventoryRepository.findByWarehouseIdAndProductId(order.getWarehouse().getId(), item.getProduct().getId()).orElse(null);
                if (inv != null) {
                    inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - item.getQuantity()));
                    inv.setQuantity(Math.max(0, inv.getQuantity() - item.getQuantity()));
                    inventoryRepository.save(inv);
                }
            }
        }

        SalesOrder updated = salesOrderRepository.save(order);
        auditLogService.log("SalesOrder", order.getId().toString(), "ORDER_STATUS_UPDATE", username,
                "Updated order " + order.getOrderNumber() + " status from " + prevStatus + " to " + newStatus);
        return updated;
    }
}

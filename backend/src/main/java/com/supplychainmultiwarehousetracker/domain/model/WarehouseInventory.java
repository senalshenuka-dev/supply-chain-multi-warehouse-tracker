package com.supplychainmultiwarehousetracker.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInventory {

    @EmbeddedId
    private WarehouseInventoryId id;

    @MapsId("warehouseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public int getAvailableQuantity() {
        return Math.max(0, (quantity != null ? quantity : 0) - (reservedQuantity != null ? reservedQuantity : 0));
    }

    public boolean isLowStock() {
        if (product == null || product.getMinThreshold() == null) {
            return false;
        }
        return (quantity != null ? quantity : 0) < product.getMinThreshold();
    }
}

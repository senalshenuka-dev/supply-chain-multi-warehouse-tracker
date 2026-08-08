package com.supplychainmultiwarehousetracker.domain.repository;

import com.supplychainmultiwarehousetracker.domain.model.WarehouseInventory;
import com.supplychainmultiwarehousetracker.domain.model.WarehouseInventoryId;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, WarehouseInventoryId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.id.warehouseId = :warehouseId AND wi.id.productId = :productId")
    Optional<WarehouseInventory> findByWarehouseIdAndProductIdWithLock(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.id.warehouseId = :warehouseId AND wi.id.productId = :productId")
    Optional<WarehouseInventory> findByWarehouseIdAndProductId(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    Optional<WarehouseInventory> findByIdWarehouseIdAndIdProductId(Long warehouseId, Long productId);

    @Query("SELECT wi FROM WarehouseInventory wi " +
           "JOIN FETCH wi.warehouse w " +
           "JOIN FETCH wi.product p " +
           "WHERE (:warehouseId IS NULL OR w.id = :warehouseId) " +
           "AND (:query IS NULL OR :query = '' OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<WarehouseInventory> findInventoryFiltered(
            @Param("warehouseId") Long warehouseId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT wi FROM WarehouseInventory wi " +
           "JOIN FETCH wi.warehouse w " +
           "JOIN FETCH wi.product p " +
           "WHERE wi.quantity < p.minThreshold")
    List<WarehouseInventory> findLowStockInventory();

    @Query("SELECT wi FROM WarehouseInventory wi " +
           "JOIN FETCH wi.warehouse w " +
           "JOIN FETCH wi.product p " +
           "WHERE (:warehouseId IS NULL OR w.id = :warehouseId) " +
           "AND wi.quantity < p.minThreshold")
    Page<WarehouseInventory> findLowStockInventoryByWarehouse(
            @Param("warehouseId") Long warehouseId,
            Pageable pageable
    );
}

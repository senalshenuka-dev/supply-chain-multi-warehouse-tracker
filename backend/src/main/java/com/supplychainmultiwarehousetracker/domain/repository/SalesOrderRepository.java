package com.supplychainmultiwarehousetracker.domain.repository;

import com.supplychainmultiwarehousetracker.domain.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    List<SalesOrder> findByStatus(String status);
    List<SalesOrder> findByWarehouseId(Long warehouseId);
}

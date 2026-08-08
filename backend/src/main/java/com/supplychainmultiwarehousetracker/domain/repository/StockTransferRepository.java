package com.supplychainmultiwarehousetracker.domain.repository;

import com.supplychainmultiwarehousetracker.domain.model.StockTransfer;
import com.supplychainmultiwarehousetracker.domain.model.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    Optional<StockTransfer> findByTransferNumber(String transferNumber);

    @Query("SELECT st FROM StockTransfer st " +
           "WHERE (:status IS NULL OR st.status = :status) " +
           "AND (:warehouseId IS NULL OR st.sourceWarehouse.id = :warehouseId OR st.targetWarehouse.id = :warehouseId) " +
           "ORDER BY st.createdAt DESC")
    Page<StockTransfer> findTransfersFiltered(
            @Param("status") TransferStatus status,
            @Param("warehouseId") Long warehouseId,
            Pageable pageable
    );
}

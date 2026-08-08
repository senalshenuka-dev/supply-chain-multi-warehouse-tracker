package com.supplychainmultiwarehousetracker.domain.repository;

import com.supplychainmultiwarehousetracker.domain.model.ShippingManifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingManifestRepository extends JpaRepository<ShippingManifest, Long> {
    Optional<ShippingManifest> findBySalesOrderId(Long salesOrderId);
    Optional<ShippingManifest> findByTrackingNumber(String trackingNumber);
}

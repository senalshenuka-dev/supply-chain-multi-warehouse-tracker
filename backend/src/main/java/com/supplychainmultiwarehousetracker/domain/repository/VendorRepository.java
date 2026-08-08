package com.supplychainmultiwarehousetracker.domain.repository;

import com.supplychainmultiwarehousetracker.domain.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByCode(String code);
    Optional<Vendor> findByContactEmail(String contactEmail);
}

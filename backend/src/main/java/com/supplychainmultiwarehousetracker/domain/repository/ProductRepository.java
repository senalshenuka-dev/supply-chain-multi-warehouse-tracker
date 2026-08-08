package com.supplychainmultiwarehousetracker.domain.repository;

import com.supplychainmultiwarehousetracker.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    Optional<Product> findBySkuOrBarcode(String sku, String barcode);
    boolean existsBySku(String sku);
}

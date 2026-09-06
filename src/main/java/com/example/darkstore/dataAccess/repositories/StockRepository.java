package com.example.darkstore.dataAccess.repositories;

import com.example.darkstore.dataAccess.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);

    boolean existsByProductIdAndQuantityGreaterThanEqual(UUID productId, Integer quantity);
}

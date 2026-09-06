package com.example.darkstore.dataAccess.repositories;

import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    boolean existsByNameIgnoreCase(String name);
}

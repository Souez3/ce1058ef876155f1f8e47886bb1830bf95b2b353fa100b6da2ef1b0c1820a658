package com.example.darkstore.dataAccess.repositories

import com.example.darkstore.dataAccess.models.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductRepository : JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    fun existsByNameIgnoreCase(name: String): Boolean
}

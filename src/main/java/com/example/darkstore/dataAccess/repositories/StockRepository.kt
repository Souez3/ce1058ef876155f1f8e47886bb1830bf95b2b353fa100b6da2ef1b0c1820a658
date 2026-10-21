package com.example.darkstore.dataAccess.repositories

import com.example.darkstore.dataAccess.models.Stock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StockRepository : JpaRepository<Stock, UUID> {
    fun findByProductId(productId: UUID): Stock?

    fun existsByProductId(productId: UUID): Boolean

    fun existsByProductIdAndQuantityGreaterThanEqual(productId: UUID, quantity: Int): Boolean
}

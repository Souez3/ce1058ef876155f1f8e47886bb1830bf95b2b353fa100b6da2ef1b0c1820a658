package com.example.darkstore.dataAccess.repositories

import com.example.darkstore.dataAccess.models.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, UUID> {
    fun findAllByOrderId(orderId: UUID): List<OrderItem>

    fun countByProductId(productId: UUID): Int
}

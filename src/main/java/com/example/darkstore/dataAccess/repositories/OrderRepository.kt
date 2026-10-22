package com.example.darkstore.dataAccess.repositories

import com.example.darkstore.dataAccess.models.Order
import com.example.darkstore.dataAccess.models.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<Order>

    fun findByCustomerPhone(phone: String): List<Order>

    fun findByStatus(status: OrderStatus): List<Order>
}

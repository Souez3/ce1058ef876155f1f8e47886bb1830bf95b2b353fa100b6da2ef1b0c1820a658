package com.example.darkstore.security

import com.example.darkstore.dataAccess.repositories.OrderRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component("orderSecurity")
class OrderSecurity(
    private val orderRepository: OrderRepository,
) {

    @Transactional(readOnly = true)
    fun isOwner(orderId: UUID): Boolean {
        val userId = SecurityUtils.getCurrentUserId() ?: return false
        val ownerId = orderRepository.findByIdOrNull(orderId)?.user?.id
        return ownerId == userId
    }
}

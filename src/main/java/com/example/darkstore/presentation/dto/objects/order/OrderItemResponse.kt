package com.example.darkstore.presentation.dto.objects.order

import java.math.BigDecimal
import java.util.UUID

data class OrderItemResponse(
    val id: UUID?,
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val priceAtPurchase: BigDecimal,
)

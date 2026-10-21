package com.example.darkstore.presentation.dto.objects.stock

import java.math.BigDecimal
import java.util.UUID

data class StockResponse(
    val productId: UUID,
    val productName: String,
    val quantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int,
    val purchasePrice: BigDecimal,
)

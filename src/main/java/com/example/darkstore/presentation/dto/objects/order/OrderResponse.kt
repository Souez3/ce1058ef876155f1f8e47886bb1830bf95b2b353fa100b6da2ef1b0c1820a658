package com.example.darkstore.presentation.dto.objects.order

import com.example.darkstore.dataAccess.models.OrderStatus
import com.example.darkstore.dataAccess.models.PaymentType
import java.math.BigDecimal
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val customerName: String?,
    val customerPhone: String,
    val status: OrderStatus,
    val address: DeliveryAddressDto,
    val comment: String?,
    val paymentType: PaymentType,
    val totalPrice: BigDecimal,
    val userId: UUID?,
    val items: List<OrderItemResponse>,
)

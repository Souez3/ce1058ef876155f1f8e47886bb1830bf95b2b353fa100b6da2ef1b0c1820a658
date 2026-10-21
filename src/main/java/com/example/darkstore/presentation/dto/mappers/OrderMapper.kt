package com.example.darkstore.presentation.dto.mappers

import com.example.darkstore.dataAccess.models.Order
import com.example.darkstore.dataAccess.models.OrderStatus
import com.example.darkstore.presentation.dto.objects.order.OrderCreateRequest
import com.example.darkstore.presentation.dto.objects.order.OrderResponse

fun OrderCreateRequest.toEntity(): Order {
    val order = Order()
    order.customerName = customerName
    order.customerPhone = requireNotNull(customerPhone) { "customerPhone is required" }
    order.comment = comment
    order.paymentType = requireNotNull(paymentType) { "paymentType is required" }
    order.address = requireNotNull(address) { "address is required" }.toEntity()
    order.status = OrderStatus.CREATED
    // user и totalPrice проставляет OrderService.create
    for (itemRequest in requireNotNull(items) { "items are required" }) {
        val item = itemRequest.toEntity()
        item.order = order
        order.items.add(item)
    }
    return order
}

fun Order.toResponse(): OrderResponse = OrderResponse(
    id = requireNotNull(id) { "Order is not persisted" },
    customerName = customerName,
    customerPhone = customerPhone,
    status = status,
    address = address.toDto(),
    comment = comment,
    paymentType = paymentType,
    totalPrice = totalPrice,
    userId = user?.id,
    items = items.map { it.toResponse() },
)

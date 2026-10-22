package com.example.darkstore.presentation.dto.mappers

import com.example.darkstore.dataAccess.models.OrderItem
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.presentation.dto.objects.order.OrderItemCreateRequest
import com.example.darkstore.presentation.dto.objects.order.OrderItemResponse

fun OrderItemCreateRequest.toEntity(): OrderItem {
    val item = OrderItem()
    val stub = Product()
    stub.id = requireNotNull(productId) { "productId is required" }
    item.product = stub
    item.quantity = requireNotNull(quantity) { "quantity is required" }
    return item
}

fun OrderItem.toResponse(): OrderItemResponse = OrderItemResponse(
    id = id,
    productId = requireNotNull(product.id) { "Product is not persisted" },
    productName = product.name,
    quantity = quantity,
    priceAtPurchase = priceAtPurchase,
)

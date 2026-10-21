package com.example.darkstore.presentation.dto.mappers

import com.example.darkstore.dataAccess.models.Stock
import com.example.darkstore.presentation.dto.objects.stock.StockResponse

fun Stock.toResponse(): StockResponse = StockResponse(
    productId = requireNotNull(product.id) { "Product is not persisted" },
    productName = product.name,
    quantity = quantity,
    reservedQuantity = reservedQuantity,
    availableQuantity = availableQuantity,
    purchasePrice = purchasePrice,
)

fun List<Stock>.toResponseList(): List<StockResponse> = map { it.toResponse() }

package com.example.darkstore.presentation.dto.objects.order

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class OrderItemsUpdateRequest(
    @field:NotEmpty
    @field:Valid
    val items: List<OrderItemUpdateRequest>? = null,
)

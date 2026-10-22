package com.example.darkstore.presentation.dto.objects.order

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class OrderItemUpdateRequest(
    val orderItemId: UUID? = null,

    val productId: UUID? = null,

    @field:PositiveOrZero
    val quantity: Int = 0,

    val remove: Boolean = false,
)

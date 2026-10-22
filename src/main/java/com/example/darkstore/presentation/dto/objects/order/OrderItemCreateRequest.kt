package com.example.darkstore.presentation.dto.objects.order

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.UUID

data class OrderItemCreateRequest(
    @field:NotNull
    val productId: UUID? = null,

    @field:NotNull
    @field:Positive
    val quantity: Int? = null,
)

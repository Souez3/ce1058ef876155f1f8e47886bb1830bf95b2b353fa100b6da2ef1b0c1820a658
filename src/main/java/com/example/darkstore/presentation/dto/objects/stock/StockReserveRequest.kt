package com.example.darkstore.presentation.dto.objects.stock

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class StockReserveRequest(
    @field:NotNull
    @field:Positive
    val quantity: Int? = null,
)

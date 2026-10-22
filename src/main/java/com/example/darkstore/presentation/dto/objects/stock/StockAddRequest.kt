package com.example.darkstore.presentation.dto.objects.stock

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class StockAddRequest(
    @field:NotNull
    @field:Positive
    val quantity: Int? = null,

    @field:PositiveOrZero
    val purchasePrice: BigDecimal? = null,
)

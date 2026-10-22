package com.example.darkstore.presentation.dto.objects.order

import com.example.darkstore.dataAccess.models.PaymentType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class OrderCreateRequest(
    val customerName: String? = null,

    @field:NotBlank
    val customerPhone: String? = null,

    val comment: String? = null,

    @field:NotNull
    val paymentType: PaymentType? = null,

    @field:NotNull
    @field:Valid
    val address: DeliveryAddressDto? = null,

    @field:NotEmpty
    @field:Valid
    val items: List<OrderItemCreateRequest>? = null,
)

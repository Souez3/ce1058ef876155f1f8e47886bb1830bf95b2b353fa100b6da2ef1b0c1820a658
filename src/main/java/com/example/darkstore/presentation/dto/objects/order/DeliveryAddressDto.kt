package com.example.darkstore.presentation.dto.objects.order

import jakarta.validation.constraints.NotBlank

data class DeliveryAddressDto(
    @field:NotBlank
    val city: String? = null,

    @field:NotBlank
    val street: String? = null,

    @field:NotBlank
    val house: String? = null,

    val entrance: String? = null,
    val floor: String? = null,
    val apartment: String? = null,

    // no need for MVP, no migration yet, value is accepted but not stored
    val intercom: String? = null,
)

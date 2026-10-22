package com.example.darkstore.presentation.dto.objects.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank
    val phone: String? = null,

    @field:NotBlank
    val password: String? = null,
)

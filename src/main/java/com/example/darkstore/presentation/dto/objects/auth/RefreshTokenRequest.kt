package com.example.darkstore.presentation.dto.objects.auth

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String? = null,
)

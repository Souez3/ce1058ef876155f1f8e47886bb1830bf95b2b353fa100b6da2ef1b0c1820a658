package com.example.darkstore.presentation.dto.objects.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank
    val currentPassword: String? = null,

    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val newPassword: String? = null,
)

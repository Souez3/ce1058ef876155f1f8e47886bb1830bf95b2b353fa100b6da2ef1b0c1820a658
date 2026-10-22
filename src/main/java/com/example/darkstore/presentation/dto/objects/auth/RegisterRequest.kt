package com.example.darkstore.presentation.dto.objects.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

// toString у data class печатает password: не логируйте объект целиком.
data class RegisterRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val firstName: String? = null,

    @field:Size(max = 100)
    val lastName: String? = null,

    @field:NotBlank
    @field:Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone format")
    val phone: String? = null,

    @field:Email
    val email: String? = null,

    @field:NotBlank
    @field:Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    val password: String? = null,
)

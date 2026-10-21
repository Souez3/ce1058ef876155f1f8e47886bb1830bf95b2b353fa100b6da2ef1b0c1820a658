package com.example.darkstore.presentation.dto.objects.auth

import com.example.darkstore.dataAccess.models.RoleEnum
import java.util.UUID

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val userId: UUID,
    val firstName: String,
    val role: RoleEnum,
)

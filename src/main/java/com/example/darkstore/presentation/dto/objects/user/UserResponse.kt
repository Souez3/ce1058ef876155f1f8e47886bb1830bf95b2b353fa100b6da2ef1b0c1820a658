package com.example.darkstore.presentation.dto.objects.user

import com.example.darkstore.dataAccess.models.RoleEnum
import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String?,
    val phone: String,
    val email: String?,
    val role: RoleEnum,
    val createdAt: Instant?,
)

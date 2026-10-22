package com.example.darkstore.presentation.dto.mappers

import com.example.darkstore.dataAccess.models.User
import com.example.darkstore.presentation.dto.objects.user.UserResponse

fun User.toResponse(): UserResponse = UserResponse(
    id = requireNotNull(id) { "User is not persisted" },
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    email = email,
    role = role,
    createdAt = createdAt,
)

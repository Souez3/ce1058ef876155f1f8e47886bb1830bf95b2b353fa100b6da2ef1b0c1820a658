package com.example.darkstore.presentation.dto.objects.auth;

import com.example.darkstore.dataAccess.models.RoleEnum;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String firstName,
        RoleEnum role
) {
    public static AuthResponse of(String accessToken,
                                  String refreshToken,
                                  long expiresInSeconds,
                                  UUID userId,
                                  String firstName,
                                  RoleEnum role) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, userId, firstName, role);
    }
}

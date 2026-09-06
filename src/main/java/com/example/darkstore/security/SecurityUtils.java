package com.example.darkstore.security;

import com.example.darkstore.dataAccess.models.RoleEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public class SecurityUtils {
    private SecurityUtils() {

    }

    public static Optional<UserPrincipal> getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentPrincipal().map(UserPrincipal::getId);
    }

    public static boolean hasRole(RoleEnum role) {
        return getCurrentPrincipal()
                .map(p -> p.user().getRole() == role)
                .orElse(false);
    }

    public static boolean isStaff() {
        return getCurrentPrincipal()
                .map(p -> p.user().getRole() != RoleEnum.CLIENT)
                .orElse(false);
    }
}

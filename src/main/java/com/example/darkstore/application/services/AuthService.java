package com.example.darkstore.application.services;

import com.example.darkstore.dataAccess.models.RefreshToken;
import com.example.darkstore.dataAccess.models.User;
import com.example.darkstore.dataAccess.repositories.RefreshTokenRepository;
import com.example.darkstore.presentation.dto.objects.auth.AuthResponse;
import com.example.darkstore.presentation.dto.objects.auth.LoginRequest;
import com.example.darkstore.presentation.dto.objects.auth.RegisterRequest;
import com.example.darkstore.security.JwtService;
import com.example.darkstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.jwt.refresh-ttl:30d}")
    private Duration refreshTtl;

    @Value("${app.security.jwt.access-ttl:15m}")
    private Duration accessTtl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerClient(request);
        return issueTokens(new UserPrincipal(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String phone = UserService.normalizePhone(request.getPhone());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(phone, request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return issueTokens(principal);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!stored.isActive()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(new UserPrincipal(stored.getUser()));
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    private AuthResponse issueTokens(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(principal.user());
        refreshToken.setToken(generateOpaqueToken());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTtl));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                accessTtl.toSeconds(),
                principal.getId(),
                principal.user().getFirstName(),
                principal.user().getRole());
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // require @EnableScheduling
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteExpiredBefore(Instant.now().minus(Duration.ofDays(7)));
    }
}
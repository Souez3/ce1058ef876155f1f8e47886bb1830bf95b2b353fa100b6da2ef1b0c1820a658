package com.example.darkstore.application.services

import com.example.darkstore.dataAccess.models.RefreshToken
import com.example.darkstore.dataAccess.repositories.RefreshTokenRepository
import com.example.darkstore.presentation.dto.objects.auth.AuthResponse
import com.example.darkstore.presentation.dto.objects.auth.LoginRequest
import com.example.darkstore.presentation.dto.objects.auth.RegisterRequest
import com.example.darkstore.security.JwtService
import com.example.darkstore.security.UserPrincipal
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userService: UserService,
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${app.security.jwt.refresh-ttl:30d}") private val refreshTtl: Duration,
    @Value("\${app.security.jwt.access-ttl:15m}") private val accessTtl: Duration,
) {

    private companion object {
        val SECURE_RANDOM = SecureRandom()
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val user = userService.registerClient(request)
        return issueTokens(UserPrincipal(user))
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val phone = UserService.normalizePhone(requireNotNull(request.phone) { "phone is required" })

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(phone, request.password)
        )

        return issueTokens(authentication.principal as UserPrincipal)
    }

    @Transactional(noRollbackFor = [BadCredentialsException::class])
    fun refresh(refreshTokenValue: String): AuthResponse {
        val stored = refreshTokenRepository.findByToken(refreshTokenValue)
            ?: throw BadCredentialsException("Invalid refresh token")

        if (!stored.isActive) {
            refreshTokenRepository.revokeAllByUserId(requireNotNull(stored.user.id))
            throw BadCredentialsException("Refresh token expired or revoked")
        }

        stored.revoked = true
        refreshTokenRepository.save(stored)

        return issueTokens(UserPrincipal(stored.user))
    }

    @Transactional
    fun logout(refreshTokenValue: String) {
        refreshTokenRepository.findByToken(refreshTokenValue)?.let {
            it.revoked = true
            refreshTokenRepository.save(it)
        }
    }

    private fun issueTokens(principal: UserPrincipal): AuthResponse {
        val accessToken = jwtService.generateAccessToken(principal)

        val refreshToken = RefreshToken()
        refreshToken.user = principal.user
        refreshToken.token = generateOpaqueToken()
        refreshToken.expiresAt = Instant.now().plus(refreshTtl)
        refreshTokenRepository.save(refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken.token,
            expiresInSeconds = accessTtl.toSeconds(),
            userId = requireNotNull(principal.id),
            firstName = principal.user.firstName,
            role = principal.user.role,
        )
    }

    private fun generateOpaqueToken(): String {
        val bytes = ByteArray(64)
        SECURE_RANDOM.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    fun purgeExpiredTokens() {
        refreshTokenRepository.deleteExpiredBefore(Instant.now().minus(Duration.ofDays(7)))
    }
}

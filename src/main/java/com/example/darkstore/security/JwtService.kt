package com.example.darkstore.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.security.jwt.secret}") base64Secret: String,
    @Value("\${app.security.jwt.access-ttl:15m}") private val accessTokenTtl: Duration,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret))

    fun generateAccessToken(principal: UserPrincipal): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(requireNotNull(principal.id) { "User is not persisted" }.toString())
            .claim("phone", principal.username)
            .claim("role", principal.user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTokenTtl)))
            .signWith(key)
            .compact()
    }

    fun extractUserId(token: String): UUID? =
        try {
            val subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
            subject?.let { UUID.fromString(it) }
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
}

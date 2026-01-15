package com.kappa.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.kappa.backend.config.AppConfig
import com.kappa.backend.models.UserRole
import java.util.Date
import java.util.UUID

class TokenService(private val config: AppConfig) {
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)

    fun generateAccessToken(userId: UUID, role: UserRole): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + config.accessTokenTtlSeconds * 1000)
        return JWT.create()
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withSubject(userId.toString())
            .withClaim("role", role.name)
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: UUID): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + config.refreshTokenTtlSeconds * 1000)
        return JWT.create()
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withSubject(userId.toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .build()
    }
}

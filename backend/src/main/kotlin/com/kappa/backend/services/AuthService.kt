package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import com.kappa.backend.data.Agencies
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.RefreshTokens
import com.kappa.backend.data.Users
import com.kappa.backend.models.LoginResponse
import com.kappa.backend.models.SignupRequest
import com.kappa.backend.models.UserResponse
import com.kappa.backend.models.UserRole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class AuthService(private val config: AppConfig) {
    private val tokenService = TokenService(config)

    enum class SignupFailureReason {
        USERNAME_TAKEN,
        INVALID_ROLE,
        AGENCY_REQUIRED,
        AGENCY_NOT_FOUND,
        WEAK_PASSWORD,
        INVALID_INPUT
    }

    data class SignupResult(
        val response: LoginResponse? = null,
        val failure: SignupFailureReason? = null
    )

    fun login(username: String, password: String): LoginResponse? {
        return transaction {
            val row = Users.select { Users.username eq username }.singleOrNull() ?: return@transaction null
            val passwordHash = row[Users.passwordHash]
            if (!BCrypt.checkpw(password, passwordHash)) {
                return@transaction null
            }
            val userId = row[Users.id]
            val role = UserRole.valueOf(row[Users.role])
            val accessToken = tokenService.generateAccessToken(userId, role)
            val refreshToken = tokenService.generateRefreshToken(userId)
            val expiresAt = System.currentTimeMillis() + config.refreshTokenTtlSeconds * 1000

            RefreshTokens.insert {
                it[token] = refreshToken
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.expiresAt] = expiresAt
            }

            LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = row.toUserResponse()
            )
        }
    }

    fun signup(request: SignupRequest): SignupResult {
        val username = request.username.trim()
        val email = request.email.trim()
        val password = request.password

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return SignupResult(failure = SignupFailureReason.INVALID_INPUT)
        }
        if (password.length < 6) {
            return SignupResult(failure = SignupFailureReason.WEAK_PASSWORD)
        }
        val resolvedRole = request.role ?: UserRole.USER
        if (request.role != null && request.role != UserRole.USER) {
            return SignupResult(failure = SignupFailureReason.INVALID_ROLE)
        }

        return transaction {
            if (Users.select { Users.username eq username }.any()) {
                return@transaction SignupResult(failure = SignupFailureReason.USERNAME_TAKEN)
            }

            val agencyId = request.agencyId?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            val resolvedAgencyId = if (resolvedRole == UserRole.USER) {
                when {
                    agencyId != null -> {
                        val exists = Agencies.select { Agencies.id eq agencyId }.any()
                        if (!exists) {
                            return@transaction SignupResult(failure = SignupFailureReason.AGENCY_NOT_FOUND)
                        }
                        agencyId
                    }
                    else -> {
                        val defaultAgency = Agencies.selectAll().limit(1).singleOrNull()
                            ?: return@transaction SignupResult(failure = SignupFailureReason.AGENCY_REQUIRED)
                        defaultAgency[Agencies.id]
                    }
                }
            } else {
                agencyId
            }

            val now = System.currentTimeMillis()
            val userId = UUID.randomUUID()
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

            Users.insert {
                it[id] = userId
                it[Users.username] = username
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.role] = resolvedRole.name
                it[Users.agencyId] = resolvedAgencyId
                it[Users.status] = "active"
                it[Users.createdAt] = now
            }

            CoinWallets.insert {
                it[CoinWallets.userId] = userId
                it[CoinWallets.balance] = 0L
                it[CoinWallets.updatedAt] = now
            }

            val accessToken = tokenService.generateAccessToken(userId, resolvedRole)
            val refreshToken = tokenService.generateRefreshToken(userId)
            val expiresAt = now + config.refreshTokenTtlSeconds * 1000

            RefreshTokens.insert {
                it[token] = refreshToken
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.expiresAt] = expiresAt
            }

            val userResponse = UserResponse(
                id = userId.toString(),
                username = username,
                email = email,
                role = resolvedRole,
                agencyId = resolvedAgencyId?.toString()
            )

            SignupResult(
                response = LoginResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = userResponse
                )
            )
        }
    }

    fun refresh(refreshToken: String): LoginResponse? {
        return transaction {
            val now = System.currentTimeMillis()
            val tokenRow = RefreshTokens.select { RefreshTokens.token eq refreshToken }.singleOrNull()
                ?: return@transaction null
            if (tokenRow[RefreshTokens.expiresAt] < now) {
                RefreshTokens.deleteWhere { RefreshTokens.token eq refreshToken }
                return@transaction null
            }

            val userId = tokenRow[RefreshTokens.userId]
            val userRow = Users.select { Users.id eq userId }.singleOrNull() ?: return@transaction null
            val role = UserRole.valueOf(userRow[Users.role])
            val accessToken = tokenService.generateAccessToken(userId, role)
            val newRefreshToken = tokenService.generateRefreshToken(userId)
            val newExpiresAt = now + config.refreshTokenTtlSeconds * 1000

            RefreshTokens.deleteWhere { RefreshTokens.token eq refreshToken }
            RefreshTokens.insert {
                it[token] = newRefreshToken
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.expiresAt] = newExpiresAt
            }

            LoginResponse(
                accessToken = accessToken,
                refreshToken = newRefreshToken,
                user = userRow.toUserResponse()
            )
        }
    }

    fun logout(refreshToken: String): Boolean {
        return transaction {
            RefreshTokens.deleteWhere { RefreshTokens.token eq refreshToken } > 0
        }
    }

    fun getUserById(userId: UUID): UserResponse? {
        return transaction {
            val row = Users.select { Users.id eq userId }.singleOrNull() ?: return@transaction null
            row.toUserResponse()
        }
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toUserResponse(): UserResponse {
        return UserResponse(
            id = this[Users.id].toString(),
            username = this[Users.username],
            email = this[Users.email],
            role = UserRole.valueOf(this[Users.role]),
            agencyId = this[Users.agencyId]?.toString()
        )
    }
}

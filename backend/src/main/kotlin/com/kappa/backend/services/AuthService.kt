package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import com.kappa.backend.data.Agencies
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.PhoneOtps
import com.kappa.backend.data.RefreshTokens
import com.kappa.backend.data.Users
import com.kappa.backend.models.GuestLoginRequest
import com.kappa.backend.models.LoginResponse
import com.kappa.backend.models.PhoneOtpRequest
import com.kappa.backend.models.PhoneOtpResponse
import com.kappa.backend.models.PhoneOtpVerifyRequest
import com.kappa.backend.models.ProfileUpdateRequest
import com.kappa.backend.models.SignupRequest
import com.kappa.backend.models.UserResponse
import com.kappa.backend.models.UserRole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import kotlin.random.Random
import java.util.UUID

class AuthService(private val config: AppConfig) {
    private val tokenService = TokenService(config)
    private val otpTtlMillis = 5 * 60 * 1000L

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
            val role = UserRole.fromStorage(row[Users.role])
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
                it[Users.phone] = request.phone?.trim()?.ifBlank { null }
                it[Users.nickname] = request.nickname?.trim()?.ifBlank { null }
                it[Users.avatarUrl] = request.avatarUrl?.trim()?.ifBlank { null }
                it[Users.country] = request.country?.trim()?.ifBlank { null }
                it[Users.language] = request.language?.trim()?.ifBlank { null }
                it[Users.isGuest] = false
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
                phone = request.phone?.trim()?.ifBlank { null },
                nickname = request.nickname?.trim()?.ifBlank { null },
                avatarUrl = request.avatarUrl?.trim()?.ifBlank { null },
                country = request.country?.trim()?.ifBlank { null },
                language = request.language?.trim()?.ifBlank { null },
                isGuest = false,
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
            val role = UserRole.fromStorage(userRow[Users.role])
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

    fun requestOtp(request: PhoneOtpRequest): PhoneOtpResponse? {
        val phone = request.phone.trim()
        if (phone.isBlank()) {
            return null
        }
        val code = Random.nextInt(100000, 999999).toString()
        val now = System.currentTimeMillis()
        val expiresAt = now + otpTtlMillis

        transaction {
            PhoneOtps.insert {
                it[id] = UUID.randomUUID()
                it[PhoneOtps.phone] = phone
                it[PhoneOtps.code] = code
                it[PhoneOtps.expiresAt] = expiresAt
                it[PhoneOtps.consumedAt] = null
                it[PhoneOtps.createdAt] = now
            }
        }

        return PhoneOtpResponse(
            phone = phone,
            code = code,
            expiresAt = expiresAt
        )
    }

    fun verifyOtp(request: PhoneOtpVerifyRequest): LoginResponse? {
        val phone = request.phone.trim()
        val code = request.code.trim()
        if (phone.isBlank() || code.isBlank()) {
            return null
        }

        return transaction {
            val now = System.currentTimeMillis()
            val otpRow = PhoneOtps
                .select { (PhoneOtps.phone eq phone) and PhoneOtps.consumedAt.isNull() and (PhoneOtps.expiresAt greater now) }
                .orderBy(PhoneOtps.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?: return@transaction null

            if (otpRow[PhoneOtps.code] != code) {
                return@transaction null
            }

            PhoneOtps.update({ PhoneOtps.id eq otpRow[PhoneOtps.id] }) {
                it[consumedAt] = now
            }

            val resolvedNickname = request.nickname?.trim()?.ifBlank { null }
            val resolvedAvatar = request.avatarUrl?.trim()?.ifBlank { null }
            val resolvedCountry = request.country?.trim()?.ifBlank { null }
            val resolvedLanguage = request.language?.trim()?.ifBlank { null }

            val existing = Users.select { Users.phone eq phone }.singleOrNull()
            val userId: UUID
            val role: UserRole

            if (existing == null) {
                val username = "phone_${phone.takeLast(4)}_${Random.nextInt(1000, 9999)}"
                val email = "$username@kappa.local"
                val passwordHash = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())
                userId = UUID.randomUUID()
                role = UserRole.USER

                Users.insert {
                    it[id] = userId
                    it[Users.username] = username
                    it[Users.email] = email
                    it[Users.passwordHash] = passwordHash
                    it[Users.role] = role.name
                    it[Users.phone] = phone
                    it[Users.nickname] = resolvedNickname
                    it[Users.avatarUrl] = resolvedAvatar
                    it[Users.country] = resolvedCountry
                    it[Users.language] = resolvedLanguage
                    it[Users.isGuest] = false
                    it[Users.status] = "active"
                    it[Users.createdAt] = now
                }

                CoinWallets.insert {
                    it[CoinWallets.userId] = userId
                    it[CoinWallets.balance] = 0L
                    it[CoinWallets.updatedAt] = now
                }
            } else {
                userId = existing[Users.id]
                role = UserRole.fromStorage(existing[Users.role])
                Users.update({ Users.id eq userId }) {
                    it[Users.phone] = phone
                    if (resolvedNickname != null) {
                        it[Users.nickname] = resolvedNickname
                    }
                    if (resolvedAvatar != null) {
                        it[Users.avatarUrl] = resolvedAvatar
                    }
                    if (resolvedCountry != null) {
                        it[Users.country] = resolvedCountry
                    }
                    if (resolvedLanguage != null) {
                        it[Users.language] = resolvedLanguage
                    }
                }
            }

            val accessToken = tokenService.generateAccessToken(userId, role)
            val refreshToken = tokenService.generateRefreshToken(userId)
            val expiresAt = now + config.refreshTokenTtlSeconds * 1000

            RefreshTokens.insert {
                it[token] = refreshToken
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.expiresAt] = expiresAt
            }

            val userRow = Users.select { Users.id eq userId }.single()
            LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = userRow.toUserResponse()
            )
        }
    }

    fun guestLogin(request: GuestLoginRequest): LoginResponse {
        return transaction {
            val now = System.currentTimeMillis()
            val username = "guest_${Random.nextInt(100000, 999999)}"
            val email = "$username@kappa.local"
            val passwordHash = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())
            val userId = UUID.randomUUID()
            val role = UserRole.USER

                Users.insert {
                    it[id] = userId
                    it[Users.username] = username
                    it[Users.email] = email
                    it[Users.passwordHash] = passwordHash
                    it[Users.role] = role.name
                    it[Users.nickname] = request.nickname?.trim()?.ifBlank { null }
                    it[Users.avatarUrl] = request.avatarUrl?.trim()?.ifBlank { null }
                    it[Users.country] = request.country?.trim()?.ifBlank { null }
                    it[Users.language] = request.language?.trim()?.ifBlank { null }
                    it[Users.isGuest] = true
                    it[Users.status] = "active"
                    it[Users.createdAt] = now
                }

            CoinWallets.insert {
                it[CoinWallets.userId] = userId
                it[CoinWallets.balance] = 0L
                it[CoinWallets.updatedAt] = now
            }

            val accessToken = tokenService.generateAccessToken(userId, role)
            val refreshToken = tokenService.generateRefreshToken(userId)
            val expiresAt = now + config.refreshTokenTtlSeconds * 1000

            RefreshTokens.insert {
                it[token] = refreshToken
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.expiresAt] = expiresAt
            }

            val userRow = Users.select { Users.id eq userId }.single()
            LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = userRow.toUserResponse()
            )
        }
    }

    fun updateProfile(userId: UUID, request: ProfileUpdateRequest): UserResponse? {
        val nickname = request.nickname?.trim()?.ifBlank { null }
        val avatarUrl = request.avatarUrl?.trim()?.ifBlank { null }
        val country = request.country?.trim()?.ifBlank { null }
        val language = request.language?.trim()?.ifBlank { null }
        return transaction {
            if (nickname != null || avatarUrl != null || country != null || language != null) {
                Users.update({ Users.id eq userId }) {
                    if (nickname != null) {
                        it[Users.nickname] = nickname
                    }
                    if (avatarUrl != null) {
                        it[Users.avatarUrl] = avatarUrl
                    }
                    if (country != null) {
                        it[Users.country] = country
                    }
                    if (language != null) {
                        it[Users.language] = language
                    }
                }
            }
            val row = Users.select { Users.id eq userId }.singleOrNull() ?: return@transaction null
            row.toUserResponse()
        }
    }

    fun updateUserRole(userId: UUID, newRole: UserRole): UserResponse? {
        return transaction {
            val updated = Users.update({ Users.id eq userId }) {
                it[Users.role] = newRole.name
            }
            if (updated == 0) {
                return@transaction null
            }
            val row = Users.select { Users.id eq userId }.singleOrNull() ?: return@transaction null
            row.toUserResponse()
        }
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toUserResponse(): UserResponse {
        return UserResponse(
            id = this[Users.id].toString(),
            username = this[Users.username],
            email = this[Users.email],
            role = UserRole.fromStorage(this[Users.role]),
            phone = this[Users.phone],
            nickname = this[Users.nickname],
            avatarUrl = this[Users.avatarUrl],
            country = this[Users.country],
            language = this[Users.language],
            isGuest = this[Users.isGuest] ?: false,
            agencyId = this[Users.agencyId]?.toString()
        )
    }
}

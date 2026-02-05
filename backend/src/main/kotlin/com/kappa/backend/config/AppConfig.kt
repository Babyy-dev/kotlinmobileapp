package com.kappa.backend.config

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig

private const val DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/kappa"
private const val DEFAULT_DB_DRIVER = "org.postgresql.Driver"
private const val DEFAULT_DB_USER = "kappa"
private const val DEFAULT_DB_PASSWORD = "kappa"
private const val DEFAULT_JWT_ISSUER = "kappa"
private const val DEFAULT_JWT_AUDIENCE = "kappa-audience"
private const val DEFAULT_JWT_REALM = "kappa"
private const val DEFAULT_JWT_SECRET = "change_this_secret"
private const val DEFAULT_ACCESS_TTL = 3600L
private const val DEFAULT_REFRESH_TTL = 604800L
private const val DEFAULT_LIVEKIT_URL = "ws://10.0.2.2:7880"
private const val DEFAULT_LIVEKIT_KEY = "devkey"
private const val DEFAULT_LIVEKIT_SECRET = "devsecret"
private const val DEFAULT_LIVEKIT_TTL = 3600L
private const val DEFAULT_OTP_EXPOSE_CODE = true
private const val DEFAULT_SOCKET_HOST = "0.0.0.0"
private const val DEFAULT_SOCKET_PORT = 8081

data class AppConfig(
    val dbUrl: String,
    val dbDriver: String,
    val dbUser: String,
    val dbPassword: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val jwtRealm: String,
    val jwtSecret: String,
    val accessTokenTtlSeconds: Long,
    val refreshTokenTtlSeconds: Long,
    val livekitUrl: String,
    val livekitApiKey: String,
    val livekitApiSecret: String,
    val livekitTokenTtlSeconds: Long,
    val twilioAccountSid: String,
    val twilioApiKeySid: String,
    val twilioApiKeySecret: String,
    val twilioAuthToken: String,
    val twilioFromNumber: String,
    val otpExposeCode: Boolean,
    val socketHost: String,
    val socketPort: Int
)

fun Application.loadConfig(): AppConfig {
    val config = environment.config
    return AppConfig(
        dbUrl = config.stringOrEnv("database.url", "KAPPA_DB_URL", DEFAULT_DB_URL),
        dbDriver = config.stringOrEnv("database.driver", "KAPPA_DB_DRIVER", DEFAULT_DB_DRIVER),
        dbUser = config.stringOrEnv("database.user", "KAPPA_DB_USER", DEFAULT_DB_USER),
        dbPassword = config.stringOrEnv("database.password", "KAPPA_DB_PASSWORD", DEFAULT_DB_PASSWORD),
        jwtIssuer = config.stringOrEnv("jwt.issuer", "KAPPA_JWT_ISSUER", DEFAULT_JWT_ISSUER),
        jwtAudience = config.stringOrEnv("jwt.audience", "KAPPA_JWT_AUDIENCE", DEFAULT_JWT_AUDIENCE),
        jwtRealm = config.stringOrEnv("jwt.realm", "KAPPA_JWT_REALM", DEFAULT_JWT_REALM),
        jwtSecret = config.stringOrEnv("jwt.secret", "KAPPA_JWT_SECRET", DEFAULT_JWT_SECRET),
        accessTokenTtlSeconds = config.longOrEnv("jwt.accessTokenTtlSeconds", "KAPPA_JWT_ACCESS_TTL", DEFAULT_ACCESS_TTL),
        refreshTokenTtlSeconds = config.longOrEnv("jwt.refreshTokenTtlSeconds", "KAPPA_JWT_REFRESH_TTL", DEFAULT_REFRESH_TTL),
        livekitUrl = config.stringOrEnv("livekit.url", "KAPPA_LIVEKIT_URL", DEFAULT_LIVEKIT_URL),
        livekitApiKey = config.stringOrEnv("livekit.apiKey", "KAPPA_LIVEKIT_API_KEY", DEFAULT_LIVEKIT_KEY),
        livekitApiSecret = config.stringOrEnv("livekit.apiSecret", "KAPPA_LIVEKIT_API_SECRET", DEFAULT_LIVEKIT_SECRET),
        livekitTokenTtlSeconds = config.longOrEnv("livekit.tokenTtlSeconds", "KAPPA_LIVEKIT_TTL", DEFAULT_LIVEKIT_TTL),
        twilioAccountSid = config.stringOrEnv("twilio.accountSid", "KAPPA_TWILIO_ACCOUNT_SID", ""),
        twilioApiKeySid = config.stringOrEnv("twilio.apiKeySid", "KAPPA_TWILIO_API_KEY_SID", ""),
        twilioApiKeySecret = config.stringOrEnv("twilio.apiKeySecret", "KAPPA_TWILIO_API_KEY_SECRET", ""),
        twilioAuthToken = config.stringOrEnv("twilio.authToken", "KAPPA_TWILIO_AUTH_TOKEN", ""),
        twilioFromNumber = config.stringOrEnv("twilio.fromNumber", "KAPPA_TWILIO_FROM", ""),
        otpExposeCode = config.booleanOrEnv("otp.exposeCode", "KAPPA_OTP_EXPOSE_CODE", DEFAULT_OTP_EXPOSE_CODE),
        socketHost = config.stringOrEnv("socket.host", "KAPPA_SOCKET_HOST", DEFAULT_SOCKET_HOST),
        socketPort = config.intOrEnv("socket.port", "KAPPA_SOCKET_PORT", DEFAULT_SOCKET_PORT)
    )
}

private fun ApplicationConfig.stringOrEnv(path: String, envKey: String, defaultValue: String): String {
    val envValue = System.getenv(envKey)
    if (!envValue.isNullOrBlank()) {
        return envValue
    }
    return propertyOrNull(path)?.getString() ?: defaultValue
}

private fun ApplicationConfig.longOrEnv(path: String, envKey: String, defaultValue: Long): Long {
    val envValue = System.getenv(envKey)
    if (!envValue.isNullOrBlank()) {
        return envValue.toLongOrNull() ?: defaultValue
    }
    val configValue = propertyOrNull(path)?.getString()
    return configValue?.toLongOrNull() ?: defaultValue
}

private fun ApplicationConfig.booleanOrEnv(path: String, envKey: String, defaultValue: Boolean): Boolean {
    val envValue = System.getenv(envKey)
    if (!envValue.isNullOrBlank()) {
        return envValue.equals("true", ignoreCase = true) || envValue == "1"
    }
    val configValue = propertyOrNull(path)?.getString()
    return configValue?.toBooleanStrictOrNull() ?: defaultValue
}

private fun ApplicationConfig.intOrEnv(path: String, envKey: String, defaultValue: Int): Int {
    val envValue = System.getenv(envKey)
    if (!envValue.isNullOrBlank()) {
        return envValue.toIntOrNull() ?: defaultValue
    }
    val configValue = propertyOrNull(path)?.getString()
    return configValue?.toIntOrNull() ?: defaultValue
}

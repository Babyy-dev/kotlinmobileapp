package com.kappa.app.core.config

/**
 * Application configuration for different environments.
 */
object AppConfig {
    
    enum class Environment {
        DEV,
        PROD
    }
    
    val currentEnvironment: Environment
        get() = if (com.kappa.app.BuildConfig.DEBUG) {
            Environment.DEV
        } else {
            Environment.PROD
        }
    
    val baseUrl: String
        get() = com.kappa.app.BuildConfig.API_BASE_URL

    val livekitUrl: String
        get() = com.kappa.app.BuildConfig.LIVEKIT_URL
    
    val appName: String = "Kappa"
    val appVersion: String = com.kappa.app.BuildConfig.VERSION_NAME
    val buildType: String = if (com.kappa.app.BuildConfig.DEBUG) "Debug" else "Release"
}

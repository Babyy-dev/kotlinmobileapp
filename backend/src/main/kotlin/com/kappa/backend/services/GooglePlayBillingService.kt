package com.kappa.backend.services

import com.google.auth.oauth2.GoogleCredentials
import com.kappa.backend.config.AppConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class GooglePlayBillingService(private val config: AppConfig) {
    private val json = Json { ignoreUnknownKeys = true }

    val isEnabled: Boolean
        get() = config.googlePlayEnabled &&
            config.googlePlayPackageName.isNotBlank() &&
            config.googlePlayServiceAccountPath.isNotBlank()

    fun verifyProductPurchase(productId: String, purchaseToken: String): GooglePlayPurchase {
        if (!isEnabled) {
            throw IllegalStateException("Google Play verification not configured")
        }

        val accessToken = getAccessToken()
        val url = URL(
            "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/" +
                "${config.googlePlayPackageName}/purchases/products/$productId/tokens/$purchaseToken"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
        }

        val responseCode = connection.responseCode
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        if (responseCode !in 200..299) {
            throw IllegalArgumentException("Google Play verification failed: $responseCode")
        }
        return json.decodeFromString(GooglePlayPurchase.serializer(), body)
    }

    private fun getAccessToken(): String {
        val file = File(config.googlePlayServiceAccountPath)
        if (!file.exists()) {
            throw IllegalStateException("Service account file not found")
        }
        file.inputStream().use { input ->
            val credentials = GoogleCredentials.fromStream(input)
                .createScoped(listOf("https://www.googleapis.com/auth/androidpublisher"))
            credentials.refreshIfExpired()
            return credentials.accessToken.tokenValue
        }
    }
}

@Serializable
data class GooglePlayPurchase(
    val orderId: String? = null,
    val purchaseState: Int = 0,
    val consumptionState: Int = 0,
    val purchaseTimeMillis: Long = 0
)

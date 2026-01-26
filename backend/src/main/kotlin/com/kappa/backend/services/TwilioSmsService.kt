package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class TwilioSmsService(private val config: AppConfig) {
    private val logger = LoggerFactory.getLogger(TwilioSmsService::class.java)

    val isEnabled: Boolean
        get() = config.twilioAccountSid.isNotBlank() &&
            config.twilioFromNumber.isNotBlank() &&
            ((config.twilioApiKeySid.isNotBlank() && config.twilioApiKeySecret.isNotBlank()) ||
                config.twilioAuthToken.isNotBlank())

    fun sendOtp(phone: String, code: String): Boolean {
        if (!isEnabled) {
            logger.warn("Twilio SMS is not configured; skipping OTP send.")
            return false
        }

        val authUser = if (config.twilioApiKeySid.isNotBlank()) {
            config.twilioApiKeySid
        } else {
            config.twilioAccountSid
        }
        val authPass = if (config.twilioApiKeySecret.isNotBlank()) {
            config.twilioApiKeySecret
        } else {
            config.twilioAuthToken
        }

        val body = "To=${encode(phone)}&From=${encode(config.twilioFromNumber)}" +
            "&Body=${encode("Your Kappa verification code is $code")}"
        val url = URL("https://api.twilio.com/2010-04-01/Accounts/${config.twilioAccountSid}/Messages.json")

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            val auth = Base64.getEncoder().encodeToString("$authUser:$authPass".toByteArray(StandardCharsets.UTF_8))
            setRequestProperty("Authorization", "Basic $auth")
        }

        connection.outputStream.use { output ->
            output.write(body.toByteArray(StandardCharsets.UTF_8))
        }

        val responseCode = connection.responseCode
        if (responseCode in 200..299) {
            return true
        }

        val errorBody = readStream(connection)
        logger.error("Twilio SMS failed with status {}: {}", responseCode, errorBody)
        return false
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
    }

    private fun readStream(connection: HttpURLConnection): String {
        val stream = connection.errorStream ?: connection.inputStream ?: return ""
        return stream.bufferedReader().use { it.readText() }
    }
}

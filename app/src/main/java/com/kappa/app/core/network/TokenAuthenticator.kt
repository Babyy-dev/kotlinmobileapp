package com.kappa.app.core.network

import com.kappa.app.core.network.model.RefreshRequest
import com.kappa.app.core.storage.PreferencesManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val refreshApi: AuthRefreshApi
) : Authenticator {
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }
        if (shouldSkip(response.request)) {
            return null
        }

        return runBlocking {
            synchronized(lock) {
                val latestToken = preferencesManager.getAccessTokenOnce()
                val requestToken = response.request.header("Authorization")
                if (!latestToken.isNullOrBlank() && requestToken != "Bearer $latestToken") {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .build()
                }

                val refreshToken = preferencesManager.getRefreshTokenOnce()
                if (refreshToken.isNullOrBlank()) {
                    preferencesManager.clearAllTokens()
                    return@runBlocking null
                }

                val refreshResponse = runCatching {
                    refreshApi.refresh(RefreshRequest(refreshToken))
                }.getOrNull()

                val data = refreshResponse?.data
                if (refreshResponse == null || !refreshResponse.success || data == null) {
                    preferencesManager.clearAllTokens()
                    return@runBlocking null
                }

                preferencesManager.saveAccessToken(data.accessToken)
                preferencesManager.saveRefreshToken(data.refreshToken)
                preferencesManager.saveUserId(data.user.id)

                return@runBlocking response.request.newBuilder()
                    .header("Authorization", "Bearer ${data.accessToken}")
                    .build()
            }
        }
    }

    private fun shouldSkip(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/auth/login") ||
            path.contains("/auth/signup") ||
            path.contains("/auth/refresh") ||
            path.contains("/auth/otp") ||
            path.contains("/auth/guest")
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

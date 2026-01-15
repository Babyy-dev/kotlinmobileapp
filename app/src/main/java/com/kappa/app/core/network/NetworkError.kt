package com.kappa.app.core.network

import java.io.IOException

/**
 * Network error types.
 */
sealed class NetworkError : IOException() {
    data class HttpError(val code: Int, override val message: String) : NetworkError()
    data class ServerError(override val message: String) : NetworkError()
    object NetworkUnavailable : NetworkError() {
        override val message: String = "Network unavailable"
    }
    object Timeout : NetworkError() {
        override val message: String = "Request timeout"
    }
    data class UnknownError(val throwable: Throwable) : NetworkError() {
        override val message: String = throwable.message ?: "Unknown error"
    }
}

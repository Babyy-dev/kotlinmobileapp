package com.kappa.app.core.network

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error mapper to convert exceptions to UI-safe error models.
 */
@Singleton
class ErrorMapper @Inject constructor() {

    fun mapToNetworkError(throwable: Throwable): NetworkError {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    in 500..599 -> NetworkError.ServerError(
                        message = throwable.message() ?: "Server error"
                    )
                    else -> NetworkError.HttpError(
                        code = throwable.code(),
                        message = throwable.message() ?: "HTTP error"
                    )
                }
            }
            is ConnectException -> NetworkError.NetworkUnavailable
            is SocketException -> NetworkError.NetworkUnavailable
            is SocketTimeoutException -> NetworkError.Timeout
            is UnknownHostException -> NetworkError.NetworkUnavailable
            else -> NetworkError.UnknownError(throwable)
        }
    }

    fun mapToUserMessage(error: NetworkError): String {
        return when (error) {
            is NetworkError.HttpError -> {
                when (error.code) {
                    401 -> "Session expired. Please log in again"
                    403 -> "Access forbidden"
                    404 -> "Resource not found"
                    500 -> "Server error. Please try again later"
                    else -> error.message
                }
            }
            is NetworkError.ServerError -> "Server error. Please try again later"
            is NetworkError.NetworkUnavailable -> "No internet connection"
            is NetworkError.Timeout -> "Request timeout. Please try again"
            is NetworkError.UnknownError -> "An unexpected error occurred"
        }
    }
}

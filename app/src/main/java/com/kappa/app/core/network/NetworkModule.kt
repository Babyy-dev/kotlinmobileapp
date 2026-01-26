package com.kappa.app.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kappa.app.BuildConfig
import com.kappa.app.core.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

object NetworkConfig {
    const val TIMEOUT_SECONDS = 30L
}

fun provideOkHttpClient(
    authInterceptor: AuthInterceptor,
    tokenAuthenticator: TokenAuthenticator
): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.d("OkHttp: $message")
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    return OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
}

fun providePlainOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.d("OkHttp: $message")
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
}

fun provideGson(): Gson {
    return GsonBuilder()
        .setLenient()
        .create()
}

fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    val baseUrl = AppConfig.baseUrl

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(provideGson()))
        .build()
}

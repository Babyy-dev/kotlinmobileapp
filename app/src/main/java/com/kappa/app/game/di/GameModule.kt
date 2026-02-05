package com.kappa.app.game.di

import com.google.gson.Gson
import com.kappa.app.core.network.providePlainOkHttpClient
import com.kappa.app.game.data.GameRepository
import com.kappa.app.core.network.ApiService
import com.kappa.app.game.data.WebSocketGameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideGameOkHttpClient(): OkHttpClient {
        return providePlainOkHttpClient()
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        okHttpClient: OkHttpClient,
        gson: Gson,
        apiService: ApiService
    ): GameRepository {
        return WebSocketGameRepository(okHttpClient, gson, apiService, CoroutineScope(Dispatchers.IO))
    }
}

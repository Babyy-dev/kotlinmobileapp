package com.kappa.app.game.di

import com.google.gson.Gson
import com.kappa.app.game.data.GameRepository
import com.kappa.app.core.network.ApiService
import com.kappa.app.game.data.LiveKitGameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideGameRepository(
        gson: Gson,
        apiService: ApiService
    ): GameRepository {
        return LiveKitGameRepository(gson, apiService, CoroutineScope(Dispatchers.IO))
    }
}

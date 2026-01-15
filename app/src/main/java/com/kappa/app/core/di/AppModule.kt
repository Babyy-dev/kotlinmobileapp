package com.kappa.app.core.di

import com.kappa.app.auth.data.repository.RemoteAuthRepository
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.audio.data.repository.RemoteAudioRepository
import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.core.network.AuthInterceptor
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.provideOkHttpClient
import com.kappa.app.core.network.provideRetrofit
import com.kappa.app.economy.data.repository.RemoteEconomyRepository
import com.kappa.app.economy.domain.repository.EconomyRepository
import com.kappa.app.user.data.repository.RemoteUserRepository
import com.kappa.app.user.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        remoteAuthRepository: RemoteAuthRepository
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        remoteUserRepository: RemoteUserRepository
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindEconomyRepository(
        remoteEconomyRepository: RemoteEconomyRepository
    ): EconomyRepository
    
    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        remoteAudioRepository: RemoteAudioRepository
    ): AudioRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofitInstance(authInterceptor: AuthInterceptor): Retrofit {
        return provideRetrofit(provideOkHttpClient(authInterceptor))
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

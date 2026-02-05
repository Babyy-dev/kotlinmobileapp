package com.kappa.app.reseller.di

import com.kappa.app.reseller.data.repository.RemoteResellerRepository
import com.kappa.app.reseller.domain.repository.ResellerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ResellerModule {

    @Singleton
    @Binds
    fun bindResellerRepository(
        repository: RemoteResellerRepository
    ): ResellerRepository
}

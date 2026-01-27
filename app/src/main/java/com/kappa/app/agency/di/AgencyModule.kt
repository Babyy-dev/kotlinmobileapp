package com.kappa.app.agency.di

import com.kappa.app.agency.data.repository.RemoteAgencyRepository
import com.kappa.app.agency.domain.repository.AgencyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AgencyModule {

    @Singleton
    @Binds
    fun bindAgencyRepository(
        repository: RemoteAgencyRepository
    ): AgencyRepository
}

package com.example.offlinelocation.di

import com.example.offlinelocation.data.repository.LocationRepositoryImpl
import com.example.offlinelocation.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(repositoryImpl: LocationRepositoryImpl): LocationRepository
}
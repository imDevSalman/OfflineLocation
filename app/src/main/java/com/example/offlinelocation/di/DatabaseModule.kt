package com.example.offlinelocation.di

import android.content.Context
import androidx.room.Room
import com.example.offlinelocation.data.local.AppDatabase
import com.example.offlinelocation.data.local.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "location.db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun locationDao(database: AppDatabase): LocationDao = database.locationDao()
}
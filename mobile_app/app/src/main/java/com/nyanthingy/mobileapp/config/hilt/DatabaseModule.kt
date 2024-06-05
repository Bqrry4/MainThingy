package com.nyanthingy.mobileapp.config.hilt

import android.content.Context
import androidx.room.Room
import com.nyanthingy.mobileapp.modules.database.AppDatabase
import com.nyanthingy.mobileapp.modules.database.model.ProfileDao
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "app_db"
        ).build()
    }
    @Provides
    @Singleton
    fun provideProfileDao(appDatabase: AppDatabase) : ProfileDao{
        return appDatabase.profileDao()
    }
}
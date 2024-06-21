package com.nyanthingy.mobileapp.config.hilt

import android.content.Context
import androidx.room.Room
import com.nyanthingy.mobileapp.modules.database.AppDatabase
import com.nyanthingy.mobileapp.modules.database.profile.model.ProfileDao
import com.nyanthingy.mobileapp.modules.database.profile.repository.ProfileRepositoryDB
import com.nyanthingy.mobileapp.modules.database.virtualfence.model.VirtualFenceDao
import com.nyanthingy.mobileapp.modules.database.virtualfence.repository.VirtualFenceRepositoryDB
import com.nyanthingy.mobileapp.modules.map.virtualfences.repository.VirtualFenceRepository
import com.nyanthingy.mobileapp.modules.profile.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DatabaseModule {

    companion object {
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
        fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
            return appDatabase.profileDao()
        }

        @Provides
        @Singleton
        fun provideVirtualFenceDao(appDatabase: AppDatabase): VirtualFenceDao {
            return appDatabase.virtualFenceDao()
        }
    }

    @Binds
    fun bindProfileRepository(
        profileRepositoryDB: ProfileRepositoryDB
    ): ProfileRepository

    @Binds
    fun bindVirtualFenceRepository(
        virtualFenceRepositoryDB: VirtualFenceRepositoryDB
    ): VirtualFenceRepository
}
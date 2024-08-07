package com.nyanthingy.mobileapp.config.hilt

import com.nyanthingy.mobileapp.modules.database.virtualfence.repository.VirtualFenceRepositoryDB
import com.nyanthingy.mobileapp.modules.map.virtualfences.repository.VirtualFenceRepository
import com.nyanthingy.mobileapp.modules.server.network.ApiService
import com.nyanthingy.mobileapp.modules.server.repository.NetworkDeviceRepository
import com.nyanthingy.mobileapp.modules.server.repository.RemoteDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {
    companion object {
        @Provides
        fun provideBaseUrl(): String = "http://192.168.85.129:8080/"

        @Provides
        @Singleton
        fun provideRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
//            .addConverterFactory(
//                Json.asConverterFactory(
//                    "application/json".toMediaType()
//                )
//            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        @Provides
        @Singleton
        fun provideApiService(retrofit: Retrofit): ApiService =
            retrofit.create(ApiService::class.java)
    }

    @Binds
    fun bindRemoteDeviceRepository(
        networkDeviceRepository: NetworkDeviceRepository
    ): RemoteDeviceRepository
}
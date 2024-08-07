package com.nyanthingy.mobileapp

import android.annotation.SuppressLint
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.impl.Scheduler.MAX_SCHEDULER_LIMIT
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject


@HiltAndroidApp
class NyanThingyApp : Application(), Configuration.Provider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

//    @Inject
//    lateinit var workerFactory: HiltWorkerFactory

    @SuppressLint("RestrictedApi")
    override val workManagerConfiguration: Configuration =
        Configuration.Builder()
            .setExecutor(Dispatchers.Default.asExecutor())
            .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
            .setTaskExecutor(Dispatchers.Default.asExecutor())
            .setMaxSchedulerLimit(MAX_SCHEDULER_LIMIT)
            .build()
//        Configuration.Builder()
//        .setWorkerFactory(workerFactory)
//        .setMinimumLoggingLevel(android.util.Log.DEBUG)
//        .build()
}
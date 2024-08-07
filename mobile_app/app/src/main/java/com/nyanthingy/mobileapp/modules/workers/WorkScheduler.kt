package com.nyanthingy.mobileapp.modules.workers

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nyanthingy.mobileapp.modules.workers.worker.FenceCheckWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleFenceCheckWorker() {

        val fenceWorker = OneTimeWorkRequestBuilder<FenceCheckWorker>().build()

        WorkManager.getInstance(context).enqueue(fenceWorker)
    }
}
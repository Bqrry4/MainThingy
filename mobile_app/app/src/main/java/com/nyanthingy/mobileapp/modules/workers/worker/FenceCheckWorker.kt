package com.nyanthingy.mobileapp.modules.workers.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nyanthingy.mobileapp.MainActivity
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.commons.extensions.windowed
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.utils.minus
import com.nyanthingy.mobileapp.modules.map.virtualfences.repository.VirtualFenceRepository
import com.nyanthingy.mobileapp.modules.profile.repository.ProfileRepository
import com.nyanthingy.mobileapp.modules.server.repository.RemoteDeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.combine


@HiltWorker
class FenceCheckWorker @AssistedInject constructor(
    private val remoteDeviceRepository: RemoteDeviceRepository,
    private val virtualFenceRepository: VirtualFenceRepository,
    private val profileRepository: ProfileRepository,

    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val combinedFlow =
            profileRepository.getAll().combine(virtualFenceRepository.getAll()) { outer, inner ->
                outer to inner
            }

        combinedFlow.collect { pair ->
            val profiles = pair.first
            val fences = pair.second

            profiles.filter {
                it.macAddress != null
            }.forEach { profile ->
                remoteDeviceRepository.getLocationFlow(profile.macAddress!!, profile.secret!!, 5000)
                    .windowed(2)
                    .collect { points ->
                        fences.forEach {
                            val firstPoint = GeoPosition(
                                latitude = points.first().latitude.toDouble(),
                                longitude = points.first().longitude.toDouble()
                            )
                            val secondPoint = GeoPosition(
                                latitude = points.last().latitude.toDouble(),
                                longitude = points.last().longitude.toDouble()
                            )

                            val previousDistance = it.center - firstPoint

                            val lastDistance = it.center - secondPoint

                            if (previousDistance > it.radius && lastDistance < it.radius) {
                                sendNotification("Kitty is back", "${profile.name} entered a virtual fence")
                            }

                            if(previousDistance < it.radius && lastDistance > it.radius)
                            {
                                sendNotification("Kitty left", "${profile.name} left a virtual fence")
                            }
                        }
                    }
            }
        }
        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.cat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (SDK_INT >= O) {
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, WORK_NAME, IMPORTANCE_HIGH)

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val NOTIFICATION_CHANNEL = "fence_worker_channel"
        const val WORK_NAME = "fence_worker"
    }

}
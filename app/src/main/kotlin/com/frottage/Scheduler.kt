package com.frottage

import android.content.Context
import android.util.Log
import androidx.work.*
import androidx.work.Constraints
import androidx.work.NetworkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

fun scheduleNextUpdate(context: Context) {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val nextUpdateTime = SettingsManager.currentWallpaperSource.schedule.nextUpdateTime(now)

    val delay = Duration.between(now, nextUpdateTime).toMillis()

    val wallpaperWorkRequest =
        OneTimeWorkRequestBuilder<WallpaperWorker>()
            .addTag("wallpaper_update")
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS,
            ).build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "wallpaper_update",
        ExistingWorkPolicy.REPLACE,
        wallpaperWorkRequest,
    )

    Log.i("scheduleNextUpdate", "Next Update scheduled at: $nextUpdateTime")
}

fun cancelUpdateSchedule(context: Context) {
    WorkManager.getInstance(context).cancelAllWorkByTag("wallpaper_update")
}

class WallpaperWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            WallpaperSetter.setWallpaper(applicationContext)
            scheduleNextUpdate(applicationContext)
            // worker will never report success, because it is immediately rescheduled
            Result.success()
        }
}
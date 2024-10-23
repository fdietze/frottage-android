package com.frottage

import android.content.Context
import android.util.Log
import androidx.work.*
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

fun scheduleNextUpdate(context: Context) {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val nextUpdateTime = getNextUpdateTime(now)

    val delay = Duration.between(now, nextUpdateTime).toMillis()

    val constraints =
        Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    val wallpaperWorkRequest =
        OneTimeWorkRequestBuilder<WallpaperWorker>()
            .addTag("wallpaper_update")
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
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

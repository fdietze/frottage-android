package com.frottage

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

fun scheduleNextUpdate(context: Context) {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val nextUpdateTime = getNextUpdateTime(now, Constants.WALLPAPER_UPDATE_HOURS_UTC)
    val delay = Duration.between(now, nextUpdateTime).toMillis()

    val wallpaperWorkRequest =
        OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            ).addTag("wallpaper_update")
            .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "wallpaper_update",
        ExistingWorkPolicy.REPLACE,
        wallpaperWorkRequest,
    )
}

fun cancelUpdateSchedule(context: Context) {
    WorkManager.getInstance(context).cancelAllWorkByTag("wallpaper_update")
}

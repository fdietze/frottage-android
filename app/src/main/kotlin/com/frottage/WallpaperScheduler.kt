package com.frottage

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WallpaperScheduler {
    fun scheduleUpdates(context: Context) {
        val now = Calendar.getInstance(Constants.UTC_TIMEZONE)
        
        Constants.WALLPAPER_UPDATE_HOURS.forEach { hour ->
            val scheduledTime = Calendar.getInstance(Constants.UTC_TIMEZONE).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            if (scheduledTime.before(now)) {
                scheduledTime.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val delay = scheduledTime.timeInMillis - now.timeInMillis
            
            val wallpaperWorkRequest = OneTimeWorkRequestBuilder<WallpaperWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("wallpaper_update")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "wallpaper_update_$hour",
                ExistingWorkPolicy.REPLACE,
                wallpaperWorkRequest
            )
        }
    }

    fun cancelUpdates(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("wallpaper_update")
    }
}

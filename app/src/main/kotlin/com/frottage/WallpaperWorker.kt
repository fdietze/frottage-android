package com.frottage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpaperWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                WallpaperSetter.setWallpaper(applicationContext)
                scheduleNextUpdate(applicationContext)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.retry()
            }
        }
}

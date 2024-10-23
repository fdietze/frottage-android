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
            WallpaperSetter.setWallpaper(applicationContext)
            scheduleNextUpdate(applicationContext)
            // worker will never report success, because it is immediately rescheduled
            Result.success()
        }
}

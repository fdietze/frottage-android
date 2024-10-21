package com.frottage

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpaperWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(applicationContext)) {
            return@withContext Result.retry()
        }

        try {
            WallpaperSetter.setWallpaper(applicationContext)
            WallpaperScheduler.scheduleUpdates(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is java.io.IOException) {
                // Likely a network-related error, retry
                Result.retry()
            } else {
                // Other error, don't retry
                Result.failure(workDataOf("error" to e.message))
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}

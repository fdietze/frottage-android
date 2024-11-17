package com.frottage

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime

object WallpaperSetter {
    private const val TAG = "WallpaperSetter"

    suspend fun setWallpaper(context: Context) {
        Log.d(TAG, "Starting wallpaper update process")
        withContext(Dispatchers.IO) {
            val now = ZonedDateTime.now(ZoneId.of("UTC"))
            val wallpaperSource = SettingsManager.currentWallpaperSource

            wallpaperSource.lockScreenUrl?.let {
                val lockScreenUrl = it
                setWallpaperForScreen(
                    context,
                    lockScreenUrl,
                    WallpaperManager.FLAG_LOCK, // Lock Screen
                    wallpaperSource.schedule.imageCacheKey(lockScreenUrl, now),
                )
            }

            wallpaperSource.homeScreenUrl?.let {
                val homeScreenUrl = it
                setWallpaperForScreen(
                    context,
                    homeScreenUrl,
                    WallpaperManager.FLAG_SYSTEM, // Home screen
                    wallpaperSource.schedule.imageCacheKey(homeScreenUrl, now),
                )
            }
        }
        Log.i(TAG, "Wallpapers set successfully")
    }

    private suspend fun setWallpaperForScreen(
        context: Context,
        url: String,
        flag: Int,
        imageCacheKey: String,
    ) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val imageLoader = ImageLoader(context)
        val imageRequest = ImageRequest
            .Builder(context)
            .data(url)
            .diskCacheKey(imageCacheKey)
            .memoryCacheKey(imageCacheKey)
            .allowHardware(false)
            .build()

        Log.i(TAG, "Downloading wallpaper from $url, cachekey: $imageCacheKey")
        val image =
            (imageLoader.execute(imageRequest) as? SuccessResult)?.image
                ?: throw Exception("Failed to load image from $url")

        wallpaperManager.setBitmap(image.toBitmap(), null, true, flag)
    }
}

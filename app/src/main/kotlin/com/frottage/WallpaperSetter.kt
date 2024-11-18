package com.frottage

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
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
                    wallpaperSource.schedule.imageRequest(lockScreenUrl, now, context),
                )
            }

            wallpaperSource.homeScreenUrl?.let {
                val homeScreenUrl = it
                setWallpaperForScreen(
                    context,
                    homeScreenUrl,
                    WallpaperManager.FLAG_SYSTEM, // Home screen
                    wallpaperSource.schedule.imageRequest(homeScreenUrl, now, context),
                )
            }
        }
        Log.i(TAG, "Wallpapers set successfully")
    }

    private suspend fun setWallpaperForScreen(
        context: Context,
        url: String,
        flag: Int,
        imageRequest: ImageRequest,
    ) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val imageLoader = ImageLoader(context)

        Log.i(TAG, "Downloading wallpaper from $url, cachekey: ${imageRequest.diskCacheKey}")
        val image =
            (imageLoader.execute(imageRequest) as? SuccessResult)?.drawable
                ?: throw Exception("Failed to load image from $url")

        val bitmap = (image as android.graphics.drawable.BitmapDrawable).bitmap
        wallpaperManager.setBitmap(bitmap, null, true, flag)
    }
}

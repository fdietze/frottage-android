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

object WallpaperSetter {
    private const val TAG = "WallpaperSetter"

    suspend fun setWallpaper(context: Context) {
        Log.d(TAG, "Starting wallpaper update process")
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val wallpaperManager = WallpaperManager.getInstance(context)

            val lockScreenUrl = SettingsManager.getLockScreenUrl(context)
            val homeScreenUrl = SettingsManager.getHomeScreenUrl(context)

            // Set lock screen wallpaper
            setWallpaperForScreen(
                context,
                loader,
                wallpaperManager,
                lockScreenUrl,
                WallpaperManager.FLAG_LOCK,
                currentImageCacheKey(lockScreenUrl),
            )

            // Set home screen wallpaper
            setWallpaperForScreen(
                context,
                loader,
                wallpaperManager,
                homeScreenUrl,
                WallpaperManager.FLAG_SYSTEM,
                currentImageCacheKey(homeScreenUrl),
            )
        }
        Log.i(TAG, "Wallpapers set successfully")
    }

    private suspend fun setWallpaperForScreen(
        context: Context,
        loader: ImageLoader,
        wallpaperManager: WallpaperManager,
        url: String,
        flag: Int,
        imageCacheKey: String,
    ) {
        val request =
            ImageRequest
                .Builder(context)
                .data(url)
                .diskCacheKey(imageCacheKey)
                .memoryCacheKey(imageCacheKey)
                .allowHardware(false)
                .build()

        Log.i(TAG, "Downloading wallpaper from $url, cachekey: $imageCacheKey")
        val image =
            (loader.execute(request) as? SuccessResult)?.image
                ?: throw Exception("Failed to load image from $url")

        wallpaperManager.setBitmap(image.toBitmap(), null, true, flag)
    }
}

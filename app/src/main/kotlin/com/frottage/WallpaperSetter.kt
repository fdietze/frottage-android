package com.frottage

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
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
                .allowHardware(false) // Disable hardware bitmaps
                .build()

        Log.i(TAG, "Downloading wallpaper from $url, cachekey: $imageCacheKey")
        val result =
            (loader.execute(request) as? SuccessResult)?.drawable
                ?: throw Exception("Failed to load image from $url")

        val bitmap = (result as android.graphics.drawable.BitmapDrawable).bitmap
        wallpaperManager.setBitmap(bitmap, null, true, flag)
    }
}

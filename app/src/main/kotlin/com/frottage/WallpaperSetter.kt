package com.frottage

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperSetter {
    private const val TAG = "WallpaperSetter"

    suspend fun setWallpaper(context: Context) {
        try {
            Log.d(TAG, "Starting wallpaper update process")
            withContext(Dispatchers.IO) {
                val loader = ImageLoader(context)
                val wallpaperManager = WallpaperManager.getInstance(context)

                val lockScreenUrl = SettingsManager.getLockScreenUrl(context)
                val homeScreenUrl = SettingsManager.getHomeScreenUrl(context)

                // Set lock screen wallpaper
                setWallpaperForScreen(context, loader, wallpaperManager, lockScreenUrl, WallpaperManager.FLAG_LOCK)

                // Set home screen wallpaper
                setWallpaperForScreen(context, loader, wallpaperManager, homeScreenUrl, WallpaperManager.FLAG_SYSTEM)
            }
            Log.i(TAG, "Wallpapers set successfully")
            Toast.makeText(context, "Wallpapers set successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpapers: ${e.message}", e)
            Toast.makeText(context, "Error setting wallpapers: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun setWallpaperForScreen(
        context: Context,
        loader: ImageLoader,
        wallpaperManager: WallpaperManager,
        url: String,
        flag: Int,
    ) {
        val request =
            ImageRequest
                .Builder(context)
                .data(url)
                .allowHardware(false) // Disable hardware bitmaps
                .build()

        val result =
            (loader.execute(request) as? SuccessResult)?.drawable
                ?: throw Exception("Failed to load image from $url")

        val bitmap = (result as android.graphics.drawable.BitmapDrawable).bitmap
        wallpaperManager.setBitmap(bitmap, null, true, flag)
    }
}

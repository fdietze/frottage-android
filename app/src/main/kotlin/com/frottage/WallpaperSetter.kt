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
    private const val WALLPAPER_URL = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg"

    suspend fun setWallpaper(context: Context) {
        try {
            Log.d(TAG, "Starting wallpaper update process")
            withContext(Dispatchers.IO) {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(WALLPAPER_URL)
                    .allowHardware(false) // Disable hardware bitmaps
                    .build()

                val result = (loader.execute(request) as? SuccessResult)?.drawable
                    ?: throw Exception("Failed to load image")

                val bitmap = (result as android.graphics.drawable.BitmapDrawable).bitmap
                WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
            Log.i(TAG, "Wallpaper set successfully")
            Toast.makeText(context, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting wallpaper: ${e.message}", e)
            Toast.makeText(context, "Error setting wallpaper: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

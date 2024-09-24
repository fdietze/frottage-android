package com.example.wallpaperapp

import android.app.WallpaperManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.setWallpaperButton).setOnClickListener {
            setWallpaper()
        }
    }

    private fun setWallpaper() {
        MainScope().launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = URL("https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg")
                    val bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
                Toast.makeText(this@MainActivity, "Wallpaper set successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error setting wallpaper: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}

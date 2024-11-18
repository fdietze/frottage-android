package com.frottage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class WallpaperSource(
    val schedule: Schedule,
    val lockScreenUrl: String? = null,
    val homeScreenUrl: String? = null,
    val getCaption: (suspend () -> String)? = null,
)

val frottageWallpaperSource = WallpaperSource(
    schedule = UtcHoursSchedule(listOf(1, 7, 13, 19)),
    lockScreenUrl = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg",
    homeScreenUrl = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-homescreen-latest.jpg",
    getCaption = {
        withContext(Dispatchers.IO) {
            val jsonString =
                URL("https://fdietze.github.io/frottage/wallpapers/mobile.json").readText()
            val jsonObject = JSONObject(jsonString)
            jsonObject.getString("prompt")
        }
    }
)

val unsplashWallpaperSource = WallpaperSource(
    schedule = EveryMinuteSchedule,
    lockScreenUrl = "https://unsplash.it/1080/2400/?random",
    homeScreenUrl = "https://unsplash.it/1080/2400/?random",
)



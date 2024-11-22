package com.frottage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class WallpaperSource(
    val schedule: Schedule,
    val lockScreen: ScreenSetting?,
    val homeScreen: ScreenSetting?,
)

data class ScreenSetting(
    val url: String,
    val blurred: Boolean,
    val getCaption: (suspend () -> String)? = null,
)


val frottageWallpaperSource = WallpaperSource(
    schedule = UtcHoursSchedule(listOf(1, 7, 13, 19)),
    lockScreen = ScreenSetting(
        url = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg",
        blurred = false,
        getCaption = {
            withContext(Dispatchers.IO) {
                val jsonString =
                    URL("https://fdietze.github.io/frottage/wallpapers/mobile.json").readText()
                val jsonObject = JSONObject(jsonString)
                jsonObject.getString("prompt")
            }
        }
    ),
    homeScreen = ScreenSetting(
        url = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg",
        blurred = true,
        getCaption = {
            withContext(Dispatchers.IO) {
                val jsonString =
                    URL("https://fdietze.github.io/frottage/wallpapers/mobile.json").readText()
                val jsonObject = JSONObject(jsonString)
                jsonObject.getString("prompt")
            }
        }
    ),
)

val unsplashWallpaperSource = WallpaperSource(
    schedule = EveryMinuteSchedule,
    lockScreen = ScreenSetting(
        url = "https://unsplash.it/1080/2400/?random",
        blurred = false,
    ),
    homeScreen = ScreenSetting(
        url = "https://unsplash.it/1080/2400/?random",
        blurred = true,
    ),
)



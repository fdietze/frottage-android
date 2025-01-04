package com.frottage

import android.content.Context
import android.content.res.Configuration


data class WallpaperSource(
    val schedule: Schedule,
    val lockScreen: ScreenSetting?,
    val homeScreen: ScreenSetting?,
)

data class ScreenSetting(
    val url: (Context) -> String,
    val blurred: Boolean,
)


fun isTablet(context: Context): Boolean {
    val configuration: Configuration = context.resources.configuration
    val smallestScreenWidthDp = configuration.smallestScreenWidthDp
    return smallestScreenWidthDp >= 600
}

fun isDarkTheme(context: Context): Boolean {
    return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true  // Dark Theme
        Configuration.UI_MODE_NIGHT_NO -> false  // Light Theme
        else -> true  // Default to Dark Theme
    }
}

const val baseUrl = "https://fdietze.github.io/frottage/wallpapers"

fun frottageUrl(context: Context): String {
    return if (isTablet(context)) {
        if (isDarkTheme(context)) {
            "${baseUrl}/wallpaper-desktop-latest.jpg"
        } else {
            "${baseUrl}/wallpaper-desktop-light-latest.jpg"
        }
    } else {
        "${baseUrl}/wallpaper-mobile-latest.jpg"
    }
}


val frottageWallpaperSource = WallpaperSource(
    schedule = UtcHoursSchedule(listOf(1, 7, 13, 19)),
    lockScreen = ScreenSetting(
        url = { context -> frottageUrl(context) },
        blurred = false,
    ),
    homeScreen = ScreenSetting(
        url = { context -> frottageUrl(context) },
        blurred = true,
    ),
)

val unsplashWallpaperSource = WallpaperSource(
    schedule = EveryXSecondsSchedule(15),
    lockScreen = ScreenSetting(
        url = { context -> "https://unsplash.it/1080/2400/?random" },
        blurred = false,
    ),
    homeScreen = ScreenSetting(
        url = { context -> "https://unsplash.it/1080/2400/?random" },
        blurred = true,
    ),
)



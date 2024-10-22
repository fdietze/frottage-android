package com.frottage

import java.util.TimeZone

object Constants {
    val WALLPAPER_UPDATE_HOURS_UTC = listOf(1, 7, 13, 19)
    val UTC_TIMEZONE: TimeZone = TimeZone.getTimeZone("UTC")
    const val DEFAULT_LOCK_SCREEN_WALLPAPER_URL: String = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg"
    const val DEFAULT_HOME_SCREEN_WALLPAPER_URL: String = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-homescreen-latest.jpg"
    const val PROMPT_URL: String = "https://fdietze.github.io/frottage/wallpapers/mobile.json"
}

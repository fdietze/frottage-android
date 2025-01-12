package com.frottage

import android.content.Context
import androidx.core.content.edit

object SettingsManager {
    val currentWallpaperSource = frottageWallpaperSource

    private const val PREFS_NAME = "FrottageSettings"
    private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
    private const val KEY_HOME_SCREEN_BLUR = "home_screen_blur"

    fun setScheduleIsEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_SCHEDULE_ENABLED, enabled)
        }
    }

    fun getScheduleIsEnabled(context: Context): Boolean =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SCHEDULE_ENABLED, false)

    fun setHomeScreenBlur(
        context: Context,
        enabled: Boolean,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_HOME_SCREEN_BLUR, enabled)
        }
    }

    fun getHomeScreenBlur(context: Context): Boolean =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HOME_SCREEN_BLUR, true)
}

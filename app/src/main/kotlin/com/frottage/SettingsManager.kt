package com.frottage

import android.content.Context
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "FrottageSettings"
    private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
    private const val KEY_LOCK_SCREEN_URL = "lock_screen_url"
    private const val KEY_HOME_SCREEN_URL = "home_screen_url"

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

    fun getLockScreenUrl(context: Context): String =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOCK_SCREEN_URL, null) ?: Constants.DEFAULT_LOCK_SCREEN_WALLPAPER_URL

    fun getHomeScreenUrl(context: Context): String =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HOME_SCREEN_URL, null) ?: Constants.DEFAULT_HOME_SCREEN_WALLPAPER_URL

    fun setLockScreenUrl(
        context: Context,
        url: String,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_LOCK_SCREEN_URL, url.trim())
        }
    }

    fun setHomeScreenUrl(
        context: Context,
        url: String,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_HOME_SCREEN_URL, url.trim())
        }
    }
}

package com.frottage

import android.content.Context
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "WallpaperSettings"
    private const val KEY_LOCK_SCREEN_URL = "lock_screen_url"
    private const val KEY_HOME_SCREEN_URL = "home_screen_url"

    fun getLockScreenUrl(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOCK_SCREEN_URL, null)
    }

    fun getHomeScreenUrl(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HOME_SCREEN_URL, null)
    }

    fun setLockScreenUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_LOCK_SCREEN_URL, url.trim())
        }
    }

    fun setHomeScreenUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_HOME_SCREEN_URL, url.trim())
        }
    }
}

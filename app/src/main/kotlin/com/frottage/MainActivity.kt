package com.frottage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.work.Configuration
import androidx.work.WorkManager

class MainActivity : ComponentActivity(), Configuration.Provider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WallpaperScreen()
                }
            }
        }
    }

    fun scheduleWallpaperUpdate() {
        WallpaperScheduler.scheduleUpdates(applicationContext)
    }

    fun cancelWallpaperUpdates() {
        WallpaperScheduler.cancelUpdates(applicationContext)
    }

    override val workManagerConfiguration: Configuration
        get() = Companion.workManagerConfiguration

    companion object {
        val workManagerConfiguration: Configuration
            get() = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
    }
}

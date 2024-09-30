package com.frottage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), Configuration.Provider {
    private val updateTrigger = MutableStateFlow(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeWallpaperUpdates()

        setContent {
            val triggerUpdate by updateTrigger.collectAsState()

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WallpaperScreen(triggerUpdate)
                }
            }
        }
    }

    private fun observeWallpaperUpdates() {
        lifecycleScope.launch {
            WorkManager.getInstance(applicationContext)
                .getWorkInfosByTagFlow("wallpaper_update")
                .collect { workInfoList ->
                    workInfoList.forEach { workInfo ->
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            updateTrigger.update { it + 1 }
                        }
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

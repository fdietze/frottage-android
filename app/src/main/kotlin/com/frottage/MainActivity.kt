package com.frottage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity(), Configuration.Provider {
    private val updateTrigger = MutableStateFlow(0)
    private val _promptFlow = MutableStateFlow<String?>(null)
    val promptFlow: StateFlow<String?> = _promptFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBatteryOptimizationExemption()
        observeWallpaperUpdates()

        fetchPrompt()

        setContent {
            val triggerUpdate by updateTrigger.collectAsState()
            val prompt by promptFlow.collectAsState()

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WallpaperScreen(triggerUpdate = triggerUpdate, prompt = prompt)
                }
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
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

    private fun fetchPrompt() {
        lifecycleScope.launch {
            try {
                val prompt = PromptFetcher.fetchPrompt()
                _promptFlow.value = prompt
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    companion object {
        val workManagerConfiguration: Configuration
            get() = Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
    }
}

package com.frottage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivity :
    ComponentActivity(),
    Configuration.Provider {
    private val updateTrigger = MutableStateFlow(0)
    private val _promptFlow = MutableStateFlow<String?>(null)
    val promptFlow: StateFlow<String?> = _promptFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBatteryOptimizationExemption()
        observeWallpaperUpdates()

        fetchAndSetPrompt()

        setContent {
            val triggerUpdate by updateTrigger.collectAsState()
            val prompt by promptFlow.collectAsState()

            var showSettings by remember { mutableStateOf(false) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (showSettings) {
                            SettingsScreen(
                                onSettingsSaved = {
                                    showSettings = false
                                },
                            )
                            BackHandler {
                                showSettings = false
                            }
                        } else {
                            WallpaperScreen(prompt = prompt, key = triggerUpdate)

                            FloatingActionButton(
                                onClick = { showSettings = true },
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent =
                    Intent().apply {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:$packageName")
                    }
                startActivity(intent)
            }
        }
    }

    private fun observeWallpaperUpdates() {
        lifecycleScope.launch {
            WorkManager
                .getInstance(applicationContext)
                .getWorkInfosByTagFlow("wallpaper_update")
                .collect { workInfoList ->
                    workInfoList.forEach { workInfo ->
                        Log.i("UpdateObserver", "workInfo state: ${workInfo.state} nextScheduleTime: ${workInfo.nextScheduleTimeMillis}")
                        if (workInfo.state == WorkInfo.State.ENQUEUED) {
                            updateTrigger.update { it + 1 }
                        }
                    }
                }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Companion.workManagerConfiguration

    private fun fetchAndSetPrompt() {
        lifecycleScope.launch {
            try {
                val prompt = fetchPrompt()
                _promptFlow.value = prompt
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    companion object {
        val workManagerConfiguration: Configuration
            get() =
                Configuration
                    .Builder()
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .build()
    }
}

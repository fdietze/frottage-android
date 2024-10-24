package com.frottage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.frottage.theme.AppTheme
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestBatteryOptimizationExemption()
        observeWallpaperUpdates()
        fetchAndSetPrompt()

        setContent {
            val navController = rememberNavController()
            val triggerUpdate by updateTrigger.collectAsState()
            val prompt by promptFlow.collectAsState()

            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "wallpaper") {
                        composable("wallpaper") {
                            Column(
                                modifier = Modifier.safeDrawingPadding().fillMaxSize(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(0.7f)
                                            .aspectRatio(9f / 16f)
                                            .heightIn(max = 200.dp),
                                ) {
                                    key(triggerUpdate) {
                                        CurrentWallpaper(
                                            modifier = Modifier.fillMaxSize(),
                                            onClick = { navController.navigate("fullscreen") },
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                prompt?.let {
                                    Text(
                                        text = "Prompt: $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    val context = LocalContext.current
                                    val coroutineScope = rememberCoroutineScope()
                                    var isScheduleEnabled by remember { mutableStateOf(SettingsManager.getScheduleIsEnabled(context)) }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text("Enable schedule")
                                        Switch(
                                            checked = isScheduleEnabled,
                                            onCheckedChange = { enabled ->
                                                isScheduleEnabled = enabled
                                                SettingsManager.setScheduleIsEnabled(context, isScheduleEnabled)
                                                if (enabled) {
                                                    coroutineScope.launch {
                                                        try {
                                                            WallpaperSetter.setWallpaper(context)
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                        scheduleNextUpdate(context)
                                                    }
                                                } else {
                                                    cancelUpdateSchedule(context)
                                                }
                                            },
                                        )
                                    }
                                    if (isScheduleEnabled) {
                                        NextUpdateTime(key = triggerUpdate)
                                    }
                                }
                            }
                            Box(modifier = Modifier.fillMaxSize()) {
                                FloatingActionButton(
                                    onClick = { navController.navigate("settings") },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                                ) {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                                }
                            }
                        }
                        composable("settings") {
                            SettingsScreen(onSettingsSaved = {
                                navController.popBackStack()
                                handleSettingsSaved()
                            })
                        }
                        composable("fullscreen") {
                            FullscreenImageScreen(onClick = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }

    private fun handleSettingsSaved() {
        val context = applicationContext
        if (SettingsManager.getScheduleIsEnabled(context)) {
            lifecycleScope.launch {
                try {
                    WallpaperSetter.setWallpaper(context)
                    updateTrigger.update { it + 1 }
                    scheduleNextUpdate(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        if (workInfo.state == WorkInfo.State.ENQUEUED) {
                            updateTrigger.update { it + 1 }
                        }
                    }
                }
        }
    }

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

    override val workManagerConfiguration: Configuration
        get() = Companion.workManagerConfiguration

    companion object {
        val workManagerConfiguration: Configuration
            get() = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}

@Composable
fun FullscreenImageScreen(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current
        val url = SettingsManager.getLockScreenUrl(context)
        val imageCacheKey = currentImageCacheKey(url)

        Box(modifier = Modifier.fillMaxSize().clickable { onClick() }) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(context)
                        .data(url)
                        .diskCacheKey(imageCacheKey)
                        .memoryCacheKey(imageCacheKey)
                        .allowHardware(false)
                        .build(),
                contentDescription = "Current Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

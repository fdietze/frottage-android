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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.frottage.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity :
    ComponentActivity(),
    Configuration.Provider {
    private val updateTrigger = MutableStateFlow(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestBatteryOptimizationExemption()
        observeWallpaperUpdates()

        setContent {
            val navController = rememberNavController()
            val triggerUpdate by updateTrigger.collectAsState()

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "wallpaper") {
                        composable("wallpaper") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    key(triggerUpdate) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            val context = LocalContext.current
                                            val wallpaperSource =
                                                SettingsManager.currentWallpaperSource
                                            wallpaperSource.lockScreen?.let {
                                                val lockScreenUrl = it.url
                                                val now = ZonedDateTime.now(ZoneId.of("UTC"))
                                                val imageRequest =
                                                    wallpaperSource.schedule.imageRequest(
                                                        lockScreenUrl,
                                                        now,
                                                        context
                                                    )
                                                AsyncImage(
                                                    model = imageRequest,
                                                    contentDescription = "Current Lock Screen Wallpaper",
                                                    modifier =
                                                    Modifier
                                                        .align(Alignment.Center)
                                                        .clip(shape = RoundedCornerShape(16.dp))
                                                        .clickable(onClick = {
                                                            navController.navigate("fullscreen")
                                                        }),
                                                    contentScale = ContentScale.Fit,
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        val context = LocalContext.current
                                        val coroutineScope = rememberCoroutineScope()
                                        var isScheduleEnabled by remember {
                                            mutableStateOf(
                                                SettingsManager.getScheduleIsEnabled(context)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text("Enable schedule")
                                            Switch(
                                                checked = isScheduleEnabled,
                                                onCheckedChange = { enabled ->
                                                    isScheduleEnabled = enabled
                                                    SettingsManager.setScheduleIsEnabled(
                                                        context,
                                                        isScheduleEnabled
                                                    )
                                                    if (enabled) {
                                                        coroutineScope.launch {
                                                            try {
                                                                WallpaperSetter.setWallpaper(context)
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error: ${e.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
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
                                FloatingActionButton(
                                    onClick = { navController.navigate("settings") },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
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

    override val workManagerConfiguration: Configuration
        get() = Companion.workManagerConfiguration

    companion object {
        val workManagerConfiguration: Configuration
            get() = Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}

@Composable
fun NextUpdateTime(key: Any? = null) {
    val context = LocalContext.current
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val scheduledIsEnabled = remember(now) { SettingsManager.getScheduleIsEnabled(context) }

    if (scheduledIsEnabled) {
        val nextUpdateTime = SettingsManager.currentWallpaperSource.schedule.nextUpdateTime(now)

        val localNextUpdateTime = nextUpdateTime.withZoneSameInstant(ZoneId.systemDefault())
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val formattedNextUpdateTime = localNextUpdateTime.format(timeFormat)

        Text("Next update at: $formattedNextUpdateTime")
    }
}

@Composable
fun FullscreenImageScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    var alreadyClicked by remember { mutableStateOf(false) }
    val wallpaperSource =
        SettingsManager.currentWallpaperSource
    wallpaperSource.lockScreen?.let {
        val lockScreenUrl = it.url
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        val imageRequest =
            wallpaperSource.schedule.imageRequest(
                lockScreenUrl,
                now,
                context
            )
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable {
                if (!alreadyClicked) {
                    alreadyClicked = true
                    onClick()
                }
            }) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Current Lock Screen Wallpaper",
                modifier =
                Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

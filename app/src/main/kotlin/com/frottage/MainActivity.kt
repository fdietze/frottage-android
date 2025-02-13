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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.frottage.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        observeWallpaperUpdates()

        setContent {
            val navController = rememberNavController()
            val triggerUpdate by updateTrigger.collectAsState()

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(navController = navController, startDestination = "wallpaper") {
                        composable("wallpaper") {
                            Column(
                                modifier =
                                Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Preview(navController, triggerUpdate, Modifier.weight(1f))

                                Column {
                                    lockScreenEnableCheckbox()
                                    homeScreenEnableCheckbox()
                                    Box(
                                        modifier =
                                        Modifier
                                            .padding(start = 32.dp),
                                    ) {
                                        homeScreenBlurCheckbox()
                                    }
                                }

                                SetWallpaperButton()

                                Spacer(modifier = Modifier.height(32.dp))

                                NextUpdateTime(key = triggerUpdate, navController = navController)

                                scheduleSwitch(triggerUpdate)

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // SettingsButton(navController)
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
                        composable("logscreen") {
                            LogFileView(onClick = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SetWallpaperButton() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        WallpaperSetter.setWallpaper(context)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Setting Wallpaper...")
            } else {
                Text("Set Wallpaper")
            }
        }
    }

    @Composable
    private fun SettingsButton(navController: NavHostController) {
        FloatingActionButton(
            onClick = { navController.navigate("settings") },
            modifier =
            Modifier
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
            )
        }
    }

    @Composable
    private fun Preview(
        navController: NavHostController,
        triggerUpdate: Int,
        modifier: Modifier,
    ) {
        key(triggerUpdate) {
            val context = LocalContext.current
            val wallpaperSource =
                SettingsManager.currentWallpaperSource
            wallpaperSource.lockScreen?.let {
                val lockScreenUrl = it.url(context)
                val now = ZonedDateTime.now(ZoneId.of("UTC"))
                val imageRequest =
                    wallpaperSource.schedule.imageRequest(
                        lockScreenUrl,
                        now,
                        context,
                    )
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Current Lock Screen Wallpaper",
                    modifier =
                    modifier
                        .clip(shape = RoundedCornerShape(16.dp))
                        .clickable(onClick = {
                            navController.navigate("fullscreen")
                        }),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }

    @Composable
    private fun scheduleSwitch(triggerUpdate: Int) {
        val context = LocalContext.current
        var isScheduleEnabled by remember {
            mutableStateOf(
                SettingsManager.getScheduleIsEnabled(context),
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
                        isScheduleEnabled,
                    )
                    if (enabled) {
                        requestBatteryOptimizationExemption()
                        scheduleNextUpdate(context)
                    } else {
                        cancelUpdateSchedule(context)
                    }
                },
            )
        }
    }


    @Composable
    private fun homeScreenBlurCheckbox() {
        val context = LocalContext.current
        var isBlurEnabled by remember {
            mutableStateOf(
                SettingsManager.getHomeScreenBlur(context),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = isBlurEnabled,
                onCheckedChange = { enabled ->
                    isBlurEnabled = enabled
                    SettingsManager.setHomeScreenBlur(
                        context,
                        isBlurEnabled,
                    )
                },
            )
            Text("Blur home screen")
        }
    }

    @Composable
    private fun homeScreenEnableCheckbox() {
        val context = LocalContext.current
        var isHomeScreenEnabled by remember {
            mutableStateOf(
                SettingsManager.getHomeScreenEnable(context),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = isHomeScreenEnabled,
                onCheckedChange = { isChecked ->
                    isHomeScreenEnabled = isChecked
                    SettingsManager.setHomeScreenEnable(
                        context,
                        isHomeScreenEnabled,
                    )
                },
            )
            Text("Frottage my home screen")
        }
    }

    @Composable
    private fun lockScreenEnableCheckbox() {
        val context = LocalContext.current
        var isLockScreenEnabled by remember {
            mutableStateOf(
                SettingsManager.getLockScreenEnable(context),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = isLockScreenEnabled,
                onCheckedChange = { isChecked ->
                    isLockScreenEnabled = isChecked
                    SettingsManager.setLockScreenEnable(
                        context,
                        isLockScreenEnabled,
                    )
                },
            )
            Text("Frottage my lock screen")
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
fun NextUpdateTime(key: Any? = null, navController: NavHostController) {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val nextUpdateTime = SettingsManager.currentWallpaperSource.schedule.nextUpdateTime(now)
    val localNextUpdateTime = nextUpdateTime.withZoneSameInstant(ZoneId.systemDefault())
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val formattedNextUpdateTime = localNextUpdateTime.format(timeFormat)

    var tapCount by remember { mutableStateOf(0) }

    Text(
        text = "Next image: $formattedNextUpdateTime",
        modifier = Modifier.clickable {
            tapCount++
            if (tapCount >= 7) {
                navController.navigate("logscreen")
                tapCount = 0
            }
        }
    )
}

@Composable
fun FullscreenImageScreen(onClick: () -> Unit) {
    val context = LocalContext.current
    var alreadyClicked by remember { mutableStateOf(false) }
    val wallpaperSource =
        SettingsManager.currentWallpaperSource
    wallpaperSource.lockScreen?.let {
        val lockScreenUrl = it.url(context)
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        val imageRequest =
            wallpaperSource.schedule.imageRequest(
                lockScreenUrl,
                now,
                context,
            )
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .clickable {
                    if (!alreadyClicked) {
                        alreadyClicked = true
                        onClick()
                    }
                },
        ) {
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

@Composable
fun WorkInfoListScreen() {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val workInfosFlow: Flow<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData("wallpaper_update").asFlow()
    val workInfos by workInfosFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    WorkInfoList(workInfos)
}

// Composable to display a list of WorkInfo
@Composable
fun WorkInfoList(workInfos: List<WorkInfo>) {
    LazyColumn {
        items(workInfos) { workInfo ->
            WorkInfoItem(workInfo)
        }
    }
}

// Composable to display a single WorkInfo item
@Composable
fun WorkInfoItem(workInfo: WorkInfo) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "ID: ${workInfo.id}")
        Text(text = "State: ${workInfo.state}")
        Text(text = "Tags: ${workInfo.tags.joinToString()}")
    }
}

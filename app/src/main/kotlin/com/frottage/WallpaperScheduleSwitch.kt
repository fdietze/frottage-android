package com.frottage

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WallpaperScheduleSwitch() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPreferences = remember { context.getSharedPreferences("FrottagePrefs", Context.MODE_PRIVATE) }
    var isScheduleEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("scheduleEnabled", false)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Enable schedule")
        Switch(
            checked = isScheduleEnabled,
            onCheckedChange = { enabled ->
                isScheduleEnabled = enabled
                sharedPreferences.edit().putBoolean("scheduleEnabled", enabled).apply()
                if (enabled) {
                    coroutineScope.launch {
                        WallpaperSetter.setWallpaper(context)
                        (context as? MainActivity)?.scheduleWallpaperUpdate()
                    }
                } else {
                    (context as? MainActivity)?.cancelWallpaperUpdates()
                }
            }
        )
    }
}

package com.frottage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WallpaperScheduleSwitch(): Boolean {
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
                        scheduleNextUpdate(context)
                    }
                } else {
                    cancelUpdateSchedule(context)
                }
            },
        )
    }
    return isScheduleEnabled
}

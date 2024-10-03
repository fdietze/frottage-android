package com.frottage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun WallpaperScheduleSwitch(): Boolean {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentTime = remember { Calendar.getInstance(TimeZone.getTimeZone("UTC")) }
    var isScheduleEnabled by remember { mutableStateOf(TimeUtils.isUpdateScheduled(currentTime)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Enable schedule")
        Switch(
            checked = isScheduleEnabled,
            onCheckedChange = { enabled ->
                isScheduleEnabled = enabled
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
    return isScheduleEnabled
}

package com.frottage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NextUpdateTime(key: Any? = null) {
    val context = LocalContext.current
    val currentTime = remember(key) { ZonedDateTime.now(ZoneId.of("UTC")) }
    val isUpdateScheduled = remember(currentTime) { SettingsManager.getScheduleIsEnabled(context) }

    if (isUpdateScheduled) {
        val nextUpdateTime = remember(currentTime) { getNextUpdateTime(currentTime) }

        val localNextUpdateTime = nextUpdateTime.withZoneSameInstant(ZoneId.systemDefault())
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val formattedNextUpdateTime = localNextUpdateTime.format(timeFormat)

        Text("Next update at: $formattedNextUpdateTime")
    }
}

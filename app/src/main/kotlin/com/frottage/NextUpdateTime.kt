package com.frottage

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*
import com.frottage.TimeUtils

@Composable
fun NextUpdateTime() {
    val sharedPreferences = LocalContext.current.getSharedPreferences("FrottagePrefs", Context.MODE_PRIVATE)
    val isScheduleEnabled = sharedPreferences.getBoolean("scheduleEnabled", false)

    if (isScheduleEnabled) {
        val nextUpdateTime = remember { TimeUtils.getNextUpdateTime() }
        val localNextUpdateTime = remember {
            nextUpdateTime.apply { timeZone = TimeZone.getDefault() }
        }

        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
        val formattedNextUpdateTime = remember { timeFormat.format(localNextUpdateTime.time) }

        Text("Next update at: $formattedNextUpdateTime")
    }
}

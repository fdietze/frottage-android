package com.frottage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.*
import com.frottage.TimeUtils

@Composable
fun NextUpdateTime(key: Any? = null) {
    val currentTime = remember(key) { Calendar.getInstance(TimeZone.getTimeZone("UTC")) }
    val isUpdateScheduled = remember(currentTime) { TimeUtils.isUpdateScheduled(currentTime) }

    if (isUpdateScheduled) {
        val nextUpdateTime = remember(currentTime) { TimeUtils.getNextUpdateTime(currentTime) }
        
        val localNextUpdateTime = nextUpdateTime.clone() as Calendar
        localNextUpdateTime.timeZone = TimeZone.getDefault()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedNextUpdateTime = timeFormat.format(localNextUpdateTime.time)

        Text("Next update at: $formattedNextUpdateTime")
    }
}

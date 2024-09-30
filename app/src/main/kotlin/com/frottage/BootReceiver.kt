package com.frottage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            if (TimeUtils.isUpdateScheduled(currentTime)) {
                WallpaperScheduler.scheduleUpdates(context)
            }
        }
    }
}

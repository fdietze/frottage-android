package com.frottage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPreferences = context.getSharedPreferences("FrottagePrefs", Context.MODE_PRIVATE)
            val isScheduleEnabled = sharedPreferences.getBoolean("scheduleEnabled", false)
            if (isScheduleEnabled) {
                WallpaperScheduler.scheduleUpdates(context)
            }
        }
    }
}

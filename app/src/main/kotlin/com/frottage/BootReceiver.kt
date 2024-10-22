package com.frottage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Schedule updates on device boot

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduleIsEnabled = SettingsManager.getScheduleIsEnabled(context)
            if (scheduleIsEnabled) {
                scheduleNextUpdate(context)
            }
        }
    }
}

package com.frottage

import java.util.Calendar
import java.util.TimeZone

object TimeUtils {
    fun getNextUpdateTime(currentTime: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))): Calendar {
        val nextUpdateHour = Constants.WALLPAPER_UPDATE_HOURS.find { it > currentTime.get(Calendar.HOUR_OF_DAY) } 
            ?: Constants.WALLPAPER_UPDATE_HOURS.first()
        
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = currentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, nextUpdateHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(currentTime)) add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    fun isUpdateScheduled(currentTime: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))): Boolean {
        val lastUpdateHour = Constants.WALLPAPER_UPDATE_HOURS.findLast { it <= currentTime.get(Calendar.HOUR_OF_DAY) }
            ?: Constants.WALLPAPER_UPDATE_HOURS.last()
        
        val lastUpdateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = currentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, lastUpdateHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (after(currentTime)) add(Calendar.DAY_OF_YEAR, -1)
        }

        return currentTime.timeInMillis - lastUpdateTime.timeInMillis <= 24 * 60 * 60 * 1000
    }
}

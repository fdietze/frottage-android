package com.frottage

import java.util.Calendar

object TimeUtils {
    fun getNextUpdateTime(): Calendar {
        val now = Calendar.getInstance(Constants.UTC_TIMEZONE)
        val nextUpdateHour = Constants.WALLPAPER_UPDATE_HOURS.find { it > now.get(Calendar.HOUR_OF_DAY) } 
            ?: Constants.WALLPAPER_UPDATE_HOURS.first()
        
        return Calendar.getInstance(Constants.UTC_TIMEZONE).apply {
            set(Calendar.HOUR_OF_DAY, nextUpdateHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

package com.frottage

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun getNextUpdateTime(
    currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    updateHours: List<Int> = Constants.WALLPAPER_UPDATE_HOURS_UTC,
): ZonedDateTime {
    val nextUpdateHour = updateHours.firstOrNull { it > currentTime.hour } ?: updateHours.first()
    val nextUpdateTime = currentTime.withHour(nextUpdateHour).truncatedTo(ChronoUnit.HOURS)
    return if (nextUpdateHour > currentTime.hour) nextUpdateTime else nextUpdateTime.plusDays(1)
}

package com.frottage

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun getNextUpdateTime(
    currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    updateHours: List<Int> = Constants.WALLPAPER_UPDATE_HOURS_UTC,
): ZonedDateTime {
    val nextUpdateHour = updateHours.firstOrNull { it > currentTime.hour } ?: updateHours.first()
    val nextUpdateTime = currentTime.withHour(nextUpdateHour).truncatedTo(ChronoUnit.HOURS)
    return if (nextUpdateHour > currentTime.hour) nextUpdateTime else nextUpdateTime.plusDays(1)
}

fun getPrevUpdateTime(
    currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    updateHours: List<Int> = Constants.WALLPAPER_UPDATE_HOURS_UTC,
): ZonedDateTime {
    val prevUpdateHour = updateHours.lastOrNull { it < currentTime.hour } ?: updateHours.last()
    val prevUpdateTime = currentTime.withHour(prevUpdateHour).truncatedTo(ChronoUnit.HOURS)
    return if (prevUpdateHour < currentTime.hour) prevUpdateTime else prevUpdateTime.minusDays(1)
}

// to debug:
// fun getNextUpdateTime(
//     currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
//     updateHours: List<Int> = Constants.WALLPAPER_UPDATE_HOURS_UTC,
// ): ZonedDateTime = currentTime.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)
//
// fun getPrevUpdateTime(
//     currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
//     updateHours: List<Int> = Constants.WALLPAPER_UPDATE_HOURS_UTC,
// ): ZonedDateTime = currentTime.truncatedTo(ChronoUnit.MINUTES)

fun zonedDateTimeToStringKey(
    dateTime: ZonedDateTime,
    pattern: String = "yyyy-MM-dd_HH-mm-ss",
): String = dateTime.format(DateTimeFormatter.ofPattern(pattern))

fun currentImageCacheKey(
    url: String,
    currentTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    updateHours: List<Int> = Constants.WALLPAPER_UPDATE_HOURS_UTC,
): String = zonedDateTimeToStringKey(getPrevUpdateTime(currentTime, updateHours)) + url

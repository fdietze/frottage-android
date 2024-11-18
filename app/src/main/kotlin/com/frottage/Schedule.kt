package com.frottage

import android.content.Context
import coil3.request.ImageRequest
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

interface Schedule {
    fun nextUpdateTime(now: ZonedDateTime): ZonedDateTime
    fun prevUpdateTime(now: ZonedDateTime): ZonedDateTime

    fun zonedDateTimeToStringKey(
        dateTime: ZonedDateTime,
        pattern: String = "yyyy-MM-dd_HH-mm-ss",
    ): String = dateTime.format(DateTimeFormatter.ofPattern(pattern))

    fun imageCacheKey(
        url: String,
        now: ZonedDateTime,
    ): String = zonedDateTimeToStringKey(prevUpdateTime(now)) + url

    fun imageRequest(
        url: String,
        now: ZonedDateTime,
        context: Context,
    ): ImageRequest {
        val imageCacheKey = imageCacheKey(url, now)
        return ImageRequest
            .Builder(context)
            .data(url)
            .diskCacheKey(imageCacheKey)
            .memoryCacheKey(imageCacheKey)
            .build()
    }
}

data class UtcHoursSchedule(val hours: List<Int>) : Schedule {
    override fun nextUpdateTime(currentTime: ZonedDateTime): ZonedDateTime {
        val nextUpdateHour = hours.firstOrNull { it > currentTime.hour } ?: hours.first()
        val nextUpdateTime = currentTime.withHour(nextUpdateHour).truncatedTo(ChronoUnit.HOURS)
        return if (nextUpdateHour > currentTime.hour) nextUpdateTime else nextUpdateTime.plusDays(1)
    }

    override fun prevUpdateTime(currentTime: ZonedDateTime): ZonedDateTime {
        val prevUpdateHour = hours.lastOrNull { it < currentTime.hour } ?: hours.last()
        val prevUpdateTime = currentTime.withHour(prevUpdateHour).truncatedTo(ChronoUnit.HOURS)
        return if (prevUpdateHour < currentTime.hour) prevUpdateTime else prevUpdateTime.minusDays(1)
    }
}

object EveryMinuteSchedule : Schedule {
    override fun nextUpdateTime(currentTime: ZonedDateTime): ZonedDateTime =
        currentTime.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)

    override fun prevUpdateTime(currentTime: ZonedDateTime): ZonedDateTime =
        currentTime.truncatedTo(ChronoUnit.MINUTES)
}
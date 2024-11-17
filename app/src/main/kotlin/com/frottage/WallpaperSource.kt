package com.frottage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class WallpaperSource(
    val schedule: Schedule,
    val lockScreenUrl: String? = null,
    val homeScreenUrl: String? = null,
    val getCaption: (suspend () -> String)? = null,
)

val frottageWallpaperSource = WallpaperSource(
    schedule = UtcHoursSchedule(listOf(1, 7, 13, 19)),
    lockScreenUrl = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-latest.jpg",
    homeScreenUrl = "https://fdietze.github.io/frottage/wallpapers/wallpaper-mobile-homescreen-latest.jpg",
    getCaption = {
        withContext(Dispatchers.IO) {
            val jsonString =
                URL("https://fdietze.github.io/frottage/wallpapers/mobile.json").readText()
            val jsonObject = JSONObject(jsonString)
            jsonObject.getString("prompt")
        }
    }
)

val unsplashWallpaperSource = WallpaperSource(
    schedule = EveryMinuteSchedule,
    lockScreenUrl = "https://unsplash.it/1080/2400/?random",
    homeScreenUrl = "https://unsplash.it/1080/2400/?random",
)


interface Schedule {
    fun nextUpdateTime(now: ZonedDateTime): ZonedDateTime
    fun prevUpdateTime(now: ZonedDateTime): ZonedDateTime

    private fun zonedDateTimeToStringKey(
        dateTime: ZonedDateTime,
        pattern: String = "yyyy-MM-dd_HH-mm-ss",
    ): String = dateTime.format(DateTimeFormatter.ofPattern(pattern))

    fun imageCacheKey(
        url: String,
        currentTime: ZonedDateTime,
    ): String = zonedDateTimeToStringKey(prevUpdateTime(currentTime)) + url
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
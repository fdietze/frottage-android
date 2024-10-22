package com.frottage

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class GetNextUpdateTimeTest {
    @Test
    fun testBeforeFirstUpdateHour() {
        val updateHours = listOf(2, 8, 14, 20)
        val currentTime = ZonedDateTime.of(2023, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC"))
        val expectedNextTime = ZonedDateTime.of(2023, 1, 1, 2, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(expectedNextTime, getNextUpdateTime(currentTime, updateHours))
    }

    @Test
    fun testBetweenUpdateHours() {
        val updateHours = listOf(2, 8, 14, 20)
        val currentTime = ZonedDateTime.of(2023, 1, 1, 10, 30, 0, 0, ZoneId.of("UTC"))
        val expectedNextTime = ZonedDateTime.of(2023, 1, 1, 14, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(expectedNextTime, getNextUpdateTime(currentTime, updateHours))
    }

    @Test
    fun testAfterLastUpdateHour() {
        val updateHours = listOf(2, 8, 14, 20)
        val currentTime = ZonedDateTime.of(2023, 1, 1, 22, 0, 0, 0, ZoneId.of("UTC"))
        val expectedNextTime = ZonedDateTime.of(2023, 1, 2, 2, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(expectedNextTime, getNextUpdateTime(currentTime, updateHours))
    }

    @Test
    fun testExactlyAtUpdateHour() {
        val updateHours = listOf(0, 12)
        val currentTime = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        val expectedNextTime = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(expectedNextTime, getNextUpdateTime(currentTime, updateHours))
    }

    @Test
    fun testOneSecondBeforeUpdateHour() {
        val updateHours = listOf(0, 12)
        val currentTime = ZonedDateTime.of(2023, 1, 1, 11, 59, 59, 0, ZoneId.of("UTC"))
        val expectedNextTime = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(expectedNextTime, getNextUpdateTime(currentTime, updateHours))
    }

    @Test
    fun testSingleUpdateHour() {
        val updateHours = listOf(15)
        val currentTime = ZonedDateTime.of(2023, 1, 1, 14, 0, 0, 0, ZoneId.of("UTC"))
        val expectedNextTime = ZonedDateTime.of(2023, 1, 1, 15, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(expectedNextTime, getNextUpdateTime(currentTime, updateHours))
    }
}

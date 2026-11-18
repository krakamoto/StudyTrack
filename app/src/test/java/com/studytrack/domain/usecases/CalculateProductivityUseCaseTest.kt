package com.studytrack.domain.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale

class CalculateProductivityUseCaseTest {

    private val calculator = CalculateProductivityUseCase()
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 1, 15)

    @Test
    fun `aggregates a simple session`() {
        val analytics = calculator(
            sessions = listOf(session(today, 10, 0, 12, 0)),
            today = today,
            zone = zone,
            locale = Locale.US
        )

        assertEquals(2 * 3_600_000L, analytics.totalTodayMillis)
        assertEquals(1, analytics.sessionsLast7Days)
        assertEquals(1, analytics.activeDaysLast7Days)
        assertEquals(2 * 3_600_000L, analytics.longestSessionMillis)
        assertEquals(10, analytics.peakHour)
        assertTrue(analytics.focusScore in 1..100)
    }

    @Test
    fun `splits a session across midnight and hour buckets`() {
        val analytics = calculator(
            sessions = listOf(session(today.minusDays(1), 23, 30, 0, 30, nextDay = today)),
            today = today,
            zone = zone,
            locale = Locale.US
        )

        val lastDay = analytics.last14Days.last()
        assertEquals(30 * 60_000L, lastDay.totalMillis)
        assertEquals(30 * 60_000L, analytics.hourlyTotals.first { it.hour == 23 }.totalMillis)
        assertEquals(30 * 60_000L, analytics.hourlyTotals.first { it.hour == 0 }.totalMillis)
        assertEquals(30 * 60_000L, analytics.totalTodayMillis)
    }

    @Test
    fun `computes week over week trend`() {
        val analytics = calculator(
            sessions = listOf(
                session(today.minusDays(10), 10, 0, 11, 0),
                session(today, 10, 0, 12, 0)
            ),
            today = today,
            zone = zone,
            locale = Locale.US
        )

        assertEquals(100, analytics.trendPercent)
    }

    @Test
    fun `computes consecutive streak`() {
        val analytics = calculator(
            sessions = listOf(
                session(today, 9, 0, 10, 0),
                session(today.minusDays(1), 9, 0, 10, 0),
                session(today.minusDays(2), 9, 0, 10, 0)
            ),
            today = today,
            zone = zone,
            locale = Locale.US
        )

        assertEquals(3, analytics.streakDays)
    }

    @Test
    fun `reaches full focus score with seven consistent two hour days`() {
        val sessions = (0..6).map { offset ->
            session(today.minusDays(offset.toLong()), 9, 0, 11, 0)
        }
        val analytics = calculator(
            sessions = sessions,
            today = today,
            zone = zone,
            locale = Locale.US
        )

        assertEquals(100, analytics.focusScore)
        assertEquals(7, analytics.activeDaysLast7Days)
        assertEquals(100, analytics.consistencyPercent)
    }

    private fun session(
        date: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        nextDay: LocalDate = date
    ): StudySession {
        val start = date.atTime(LocalTime.of(startHour, startMinute))
            .toInstant(zone)
            .toEpochMilli()
        val end = nextDay.atTime(LocalTime.of(endHour, endMinute))
            .toInstant(zone)
            .toEpochMilli()
        return StudySession(startTime = start, endTime = end)
    }
}

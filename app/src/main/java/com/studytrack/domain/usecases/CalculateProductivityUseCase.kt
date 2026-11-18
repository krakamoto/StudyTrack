package com.studytrack.domain.usecases

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Pure, testable aggregation for the Statistics screen.
 *
 * Durations are split at hour and day boundaries so a session that crosses
 * midnight or 14:00-16:00 contributes correctly to every affected bucket.
 */
class CalculateProductivityUseCase @Inject constructor() {

    operator fun invoke(
        sessions: List<StudySession>,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): ProductivityAnalytics {
        val completed = sessions.filter { it.endTime > it.startTime }
        val byDate = mutableMapOf<LocalDate, Long>()
        val last30Start = today.minusDays(29)
        val hourly = LongArray(24)
        val weekday = LongArray(7)

        completed.forEach { session ->
            var cursor = session.startTime
            while (cursor < session.endTime) {
                val dateTime = Instant.ofEpochMilli(cursor).atZone(zone)
                val day = dateTime.toLocalDate()
                val nextHour = dateTime.truncatedTo(ChronoUnit.HOURS)
                    .plusHours(1)
                    .toInstant()
                    .toEpochMilli()
                val nextDay = day.plusDays(1)
                    .atStartOfDay(zone)
                    .toInstant()
                    .toEpochMilli()
                val sliceEnd = minOf(session.endTime, nextHour, nextDay)
                val sliceMillis = sliceEnd - cursor
                if (sliceMillis <= 0L) break

                byDate[day] = (byDate[day] ?: 0L) + sliceMillis
                if (!day.isBefore(last30Start) && !day.isAfter(today)) {
                    hourly[dateTime.hour] += sliceMillis
                    weekday[day.dayOfWeek.value - 1] += sliceMillis
                }
                cursor = sliceEnd
            }
        }

        val dayFormatter = DateTimeFormatter.ofPattern("EEE", locale)
        val monthFormatter = DateTimeFormatter.ofPattern("MMM", locale)
        val weekFields = WeekFields.ISO

        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val daily = last7Days.map { day ->
            ChartPoint(
                label = day.format(dayFormatter),
                totalMillis = byDate[day] ?: 0L
            )
        }

        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeks = (7 downTo 0).map { currentWeekStart.minusWeeks(it.toLong()) }
        val weekly = weeks.map { weekStart ->
            ChartPoint(
                label = "W${weekStart.get(weekFields.weekOfWeekBasedYear())}",
                totalMillis = millisBetween(weekStart, weekStart.plusDays(6), byDate)
            )
        }

        val currentMonth = YearMonth.from(today)
        val months = (11 downTo 0).map { currentMonth.minusMonths(it.toLong()) }
        val monthly = months.map { month ->
            ChartPoint(
                label = month.atDay(1).format(monthFormatter),
                totalMillis = millisBetween(month.atDay(1), month.atEndOfMonth(), byDate)
            )
        }

        val last14Days = (13 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val movingAverageStart = day.minusDays(6)
            val movingAverage = (0..6).sumOf { back ->
                byDate[movingAverageStart.plusDays(back.toLong())] ?: 0L
            } / 7L
            TrendPoint(
                label = day.format(dayFormatter),
                date = day,
                totalMillis = byDate[day] ?: 0L,
                movingAverageMillis = movingAverage
            )
        }

        val totalLast7 = last7Days.sumOf { byDate[it] ?: 0L }
        val previous7Start = today.minusDays(13)
        val previous7End = today.minusDays(7)
        val previous7Millis = millisBetween(previous7Start, previous7End, byDate)
        val trendPercent = if (previous7Millis > 0L) {
            ((totalLast7 - previous7Millis).toDouble() / previous7Millis * 100.0).roundToInt()
        } else {
            null
        }

        val sessionsLast7 = completed.filter { session ->
            val date = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
            !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
        }
        val activeDays = last7Days.count { (byDate[it] ?: 0L) > 0L }
        val avgSessionMillis = if (sessionsLast7.isEmpty()) 0L else {
            sessionsLast7.sumOf { it.endTime - it.startTime } / sessionsLast7.size
        }
        val avgDailyMillis = totalLast7 / 7L
        val streak = computeStreak(byDate.keys, today)

        // 40% consistency, 30% volume vs. a 2h/day target, 20% session
        // quality vs. 45 minutes, 10% streak.
        val consistencyScore = activeDays / 7f * 40f
        val volumeScore = (avgDailyMillis / (2 * 3_600_000f)).coerceIn(0f, 1f) * 30f
        val sessionScore = (avgSessionMillis / (45 * 60_000f)).coerceIn(0f, 1f) * 20f
        val streakScore = (streak / 7f).coerceIn(0f, 1f) * 10f
        val focusScore = (consistencyScore + volumeScore + sessionScore + streakScore)
            .roundToInt()
            .coerceIn(0, 100)

        val hourlyTotals = hourly.mapIndexed { hour, millis ->
            HourlyPoint(hour = hour, totalMillis = millis)
        }
        val peakHourPoint = hourlyTotals.maxByOrNull { it.totalMillis }
            ?.takeIf { it.totalMillis > 0L }

        val weekdayTotals = (1..7).map { dayNumber ->
            val day = DayOfWeek.of(dayNumber)
            WeekdayPoint(
                day = day,
                label = day.getDisplayName(TextStyle.SHORT, locale),
                totalMillis = weekday[dayNumber - 1]
            )
        }
        val peakWeekdayPoint = weekdayTotals.maxByOrNull { it.totalMillis }
            ?.takeIf { it.totalMillis > 0L }

        val projectTotals = completed
            .filter { session ->
                val date = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
                !date.isBefore(last30Start) && !date.isAfter(today)
            }
            .groupBy { it.projectId }
            .map { (projectId, projectSessions) ->
                ProjectDuration(
                    projectId = projectId,
                    totalMillis = projectSessions.sumOf { it.endTime - it.startTime }
                )
            }
            .sortedByDescending { it.totalMillis }

        return ProductivityAnalytics(
            dailyTotals = daily,
            weeklyTotals = weekly,
            monthlyTotals = monthly,
            last14Days = last14Days,
            hourlyTotals = hourlyTotals,
            weekdayTotals = weekdayTotals,
            projectTotals = projectTotals,
            totalTodayMillis = byDate[today] ?: 0L,
            totalThisWeekMillis = millisBetween(currentWeekStart, today, byDate),
            totalThisMonthMillis = millisBetween(currentMonth.atDay(1), today, byDate),
            sessionsLast7Days = sessionsLast7.size,
            avgSessionMillis = avgSessionMillis,
            activeDaysLast7Days = activeDays,
            consistencyPercent = (activeDays / 7f * 100f).roundToInt(),
            trendPercent = trendPercent,
            peakHour = peakHourPoint?.hour,
            peakHourMillis = peakHourPoint?.totalMillis ?: 0L,
            peakWeekday = peakWeekdayPoint?.day,
            peakWeekdayMillis = peakWeekdayPoint?.totalMillis ?: 0L,
            focusScore = focusScore,
            bestDayMillis = byDate.values.maxOrNull() ?: 0L,
            longestSessionMillis = completed.maxOfOrNull { it.endTime - it.startTime } ?: 0L,
            streakDays = streak,
            topProjectId = projectTotals.firstOrNull()?.projectId,
            hasData = completed.isNotEmpty()
        )
    }

    private fun millisBetween(
        start: LocalDate,
        end: LocalDate,
        byDate: Map<LocalDate, Long>
    ): Long {
        if (end.isBefore(start)) return 0L
        var cursor = start
        var total = 0L
        while (!cursor.isAfter(end)) {
            total += byDate[cursor] ?: 0L
            cursor = cursor.plusDays(1)
        }
        return total
    }

    private fun computeStreak(studiedDays: Set<LocalDate>, today: LocalDate): Int {
        var cursor = when {
            today in studiedDays -> today
            today.minusDays(1) in studiedDays -> today.minusDays(1)
            else -> return 0
        }
        var streak = 0
        while (cursor in studiedDays) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}

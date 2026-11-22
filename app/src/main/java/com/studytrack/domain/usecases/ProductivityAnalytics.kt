package com.studytrack.domain.usecases

import java.time.DayOfWeek
import java.time.LocalDate

/** A zero-filled value rendered by the native charts. */
data class ChartPoint(
    val label: String,
    val totalMillis: Long
)

data class TrendPoint(
    val label: String,
    val date: LocalDate,
    val totalMillis: Long,
    val movingAverageMillis: Long
)

data class HourlyPoint(
    val hour: Int,
    val totalMillis: Long
)

data class WeekdayPoint(
    val day: DayOfWeek,
    val label: String,
    val totalMillis: Long
)

data class DailyGoalPoint(
    val label: String,
    val date: LocalDate,
    val totalMillis: Long,
    val goalMillis: Long,
    val achieved: Boolean
) {
    val progress: Float
        get() = (totalMillis.toFloat() / goalMillis.coerceAtLeast(1L)).coerceIn(0f, 1f)
}

data class ProjectDuration(
    val projectId: Long?,
    val totalMillis: Long
)

data class StudySession(
    val startTime: Long,
    val endTime: Long,
    val projectId: Long? = null
)

data class ProductivityAnalytics(
    val dailyTotals: List<ChartPoint> = emptyList(),
    val weeklyTotals: List<ChartPoint> = emptyList(),
    val monthlyTotals: List<ChartPoint> = emptyList(),
    val last14Days: List<TrendPoint> = emptyList(),
    val hourlyTotals: List<HourlyPoint> = emptyList(),
    val weekdayTotals: List<WeekdayPoint> = emptyList(),
    val dailyGoalProgress: List<DailyGoalPoint> = emptyList(),
    val projectTotals: List<ProjectDuration> = emptyList(),
    val totalTodayMillis: Long = 0L,
    val totalThisWeekMillis: Long = 0L,
    val totalThisMonthMillis: Long = 0L,
    val sessionsLast7Days: Int = 0,
    val avgSessionMillis: Long = 0L,
    val activeDaysLast7Days: Int = 0,
    val consistencyPercent: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val goalCompletionPercent: Int = 0,
    val goalMetDaysLast7Days: Int = 0,
    val goalStreakDays: Int = 0,
    val trendPercent: Int? = null,
    val peakHour: Int? = null,
    val peakHourMillis: Long = 0L,
    val peakWeekday: DayOfWeek? = null,
    val peakWeekdayMillis: Long = 0L,
    val focusScore: Int = 0,
    val bestDayMillis: Long = 0L,
    val longestSessionMillis: Long = 0L,
    val streakDays: Int = 0,
    val topProjectId: Long? = null,
    val hasData: Boolean = false
)

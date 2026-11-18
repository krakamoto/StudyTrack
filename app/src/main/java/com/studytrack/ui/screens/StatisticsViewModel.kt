package com.studytrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.dao.ProjectTotal
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.repository.StudyTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

/** A single zero-filled point shown by the native bar chart. */
data class ChartPoint(
    val label: String,
    val totalMillis: Long
)

data class StatisticsUiState(
    val selectedTab: Int = 0,                     // 0=Daily, 1=Weekly, 2=Monthly
    val dailyTotals: List<ChartPoint> = emptyList(),
    val weeklyTotals: List<ChartPoint> = emptyList(),
    val monthlyTotals: List<ChartPoint> = emptyList(),
    val projectTotals: List<ProjectTotal> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val streakDays: Int = 0,
    val avgDailyMillis: Long = 0L,
    val bestDayMillis: Long = 0L,
    val totalTodayMillis: Long = 0L,
    val totalThisWeekMillis: Long = 0L,
    val totalThisMonthMillis: Long = 0L
) {
    fun projectName(id: Long?) = projects.firstOrNull { it.id == id }?.name
    fun projectColor(id: Long?) = projects.firstOrNull { it.id == id }?.colorHex
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StudyTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

    init {
        viewModelScope.launch {
            combine(
                repository.getAllEntries(),
                repository.getAllProjects()
            ) { entries, projects -> entries to projects }
                .collect { (entries, projects) ->
                    _uiState.update { current ->
                        buildState(entries, projects, current.selectedTab)
                    }
                }
        }
    }

    private fun buildState(
        allEntries: List<com.studytrack.data.entities.TimeEntryEntity>,
        projects: List<ProjectEntity>,
        selectedTab: Int
    ): StatisticsUiState {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val completed = allEntries.filter { !it.isRunning && it.endTime != null }

        fun dateOf(entry: com.studytrack.data.entities.TimeEntryEntity): LocalDate =
            Instant.ofEpochMilli(entry.startTime).atZone(zone).toLocalDate()

        fun durationOf(entry: com.studytrack.data.entities.TimeEntryEntity): Long =
            (entry.endTime ?: entry.startTime) - entry.startTime

        val byDate = completed.groupBy(::dateOf)
        val millisByDate = byDate.mapValues { (_, entries) -> entries.sumOf(::durationOf) }

        // Last 7 calendar days, including days with no tracked time.
        val days = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
        val daily = days.map { day ->
            ChartPoint(
                label = day.format(dayLabelFormatter),
                totalMillis = millisByDate[day] ?: 0L
            )
        }

        // Last 8 calendar weeks (Monday-based), including empty weeks.
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeks = (7 downTo 0).map { offset -> currentWeekStart.minusWeeks(offset.toLong()) }
        val millisByWeek = completed.groupBy { entry ->
            dateOf(entry).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }.mapValues { (_, entries) -> entries.sumOf(::durationOf) }
        val weekFields = WeekFields.ISO
        val weekly = weeks.map { weekStart ->
            ChartPoint(
                label = "W${weekStart.get(weekFields.weekOfWeekBasedYear())}",
                totalMillis = millisByWeek[weekStart] ?: 0L
            )
        }

        // Last 12 months, including empty months.
        val currentMonth = YearMonth.now()
        val months = (11 downTo 0).map { offset -> currentMonth.minusMonths(offset.toLong()) }
        val millisByMonth = completed.groupBy { entry ->
            YearMonth.from(dateOf(entry))
        }.mapValues { (_, entries) -> entries.sumOf(::durationOf) }
        val monthly = months.map { month ->
            ChartPoint(
                label = month.atDay(1).format(monthLabelFormatter),
                totalMillis = millisByMonth[month] ?: 0L
            )
        }

        // Project breakdown for the last 30 days.
        val projectRangeStart = today.minusDays(29)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val projectTotals = completed
            .filter { it.startTime >= projectRangeStart }
            .groupBy { it.projectId }
            .map { (projectId, entries) ->
                ProjectTotal(
                    projectId = projectId,
                    totalMillis = entries.sumOf(::durationOf)
                )
            }
            .sortedByDescending { it.totalMillis }

        val studiedDays = millisByDate.keys
        val todayMillis = millisByDate[today] ?: 0L
        val thisWeekMillis = millisByWeek[currentWeekStart] ?: 0L
        val thisMonthMillis = millisByMonth[currentMonth] ?: 0L

        return StatisticsUiState(
            selectedTab = selectedTab,
            dailyTotals = daily,
            weeklyTotals = weekly,
            monthlyTotals = monthly,
            projectTotals = projectTotals,
            projects = projects,
            streakDays = computeStreak(studiedDays, today),
            avgDailyMillis = daily.sumOf { it.totalMillis } / 7L,
            bestDayMillis = millisByDate.values.maxOrNull() ?: 0L,
            totalTodayMillis = todayMillis,
            totalThisWeekMillis = thisWeekMillis,
            totalThisMonthMillis = thisMonthMillis
        )
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

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}

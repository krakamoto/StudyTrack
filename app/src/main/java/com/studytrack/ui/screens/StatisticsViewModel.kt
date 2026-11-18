package com.studytrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.dao.DailyTotal
import com.studytrack.data.dao.ProjectTotal
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.repository.StudyTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class StatisticsUiState(
    val selectedTab: Int = 0,                     // 0=Daily, 1=Weekly, 2=Monthly
    val dailyTotals: List<DailyTotal> = emptyList(),
    val weeklyTotals: List<DailyTotal> = emptyList(),
    val monthlyTotals: List<DailyTotal> = emptyList(),
    val projectTotals: List<ProjectTotal> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val streakDays: Int = 0,
    val avgDailyMillis: Long = 0L
) {
    val totalTodayMillis: Long
        get() {
            val today = todayDateString()
            return dailyTotals.firstOrNull { it.date == today }?.totalMillis ?: 0L
        }

    val totalThisWeekMillis: Long
        get() = weeklyTotals.lastOrNull()?.totalMillis ?: 0L

    val bestDayMillis: Long get() = dailyTotals.maxOfOrNull { it.totalMillis } ?: 0L

    fun projectName(id: Long?) = projects.firstOrNull { it.id == id }?.name
    fun projectColor(id: Long?) = projects.firstOrNull { it.id == id }?.colorHex
}

private fun todayDateString(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StudyTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        val now = System.currentTimeMillis()
        val msPerDay = 86_400_000L

        // Daily: last 7 days
        val sevenDaysAgo = now - 7 * msPerDay
        viewModelScope.launch {
            repository.getDailyTotals(sevenDaysAgo, now).collect { list ->
                _uiState.update { state ->
                    state.copy(
                        dailyTotals = list,
                        streakDays = computeStreak(list),
                        avgDailyMillis = if (list.isEmpty()) 0L else list.sumOf { it.totalMillis } / list.size
                    )
                }
            }
        }

        // Weekly: last 8 weeks
        val eightWeeksAgo = now - 56 * msPerDay
        viewModelScope.launch {
            repository.getWeeklyTotals(eightWeeksAgo, now).collect { list ->
                _uiState.update { it.copy(weeklyTotals = list) }
            }
        }

        // Monthly: last 12 months (~365 days)
        val twelveMonthsAgo = now - 365 * msPerDay
        viewModelScope.launch {
            repository.getDailyTotals(twelveMonthsAgo, now).collect { list ->
                // Group by month
                val monthly = list.groupBy { it.date.substring(0, 7) }
                    .map { (month, days) ->
                        DailyTotal(date = month, totalMillis = days.sumOf { it.totalMillis })
                    }
                    .sortedBy { it.date }
                _uiState.update { it.copy(monthlyTotals = monthly) }
            }
        }

        // Project totals: last 30 days
        val thirtyDaysAgo = now - 30 * msPerDay
        viewModelScope.launch {
            repository.getProjectTotals(thirtyDaysAgo, now).collect { list ->
                _uiState.update { it.copy(projectTotals = list) }
            }
        }

        viewModelScope.launch {
            repository.getAllProjects().collect { list ->
                _uiState.update { it.copy(projects = list) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    private fun computeStreak(dailyTotals: List<DailyTotal>): Int {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = dailyTotals.map { it.date }.toHashSet()
        var streak = 0
        val cal = Calendar.getInstance()
        while (true) {
            val d = sdf.format(cal.time)
            if (!dates.contains(d)) break
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }
}

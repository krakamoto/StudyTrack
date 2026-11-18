package com.studytrack.domain.usecases

import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.domain.models.DayTotal
import com.studytrack.domain.models.ProjectStudyTotal
import com.studytrack.domain.models.Statistics
import com.studytrack.domain.models.WeekTotal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combines daily totals, weekly totals, and project totals into a single
 * [Statistics] flow for the given date range.
 *
 * @param fromMillis  Start of range (epoch millis, inclusive)
 * @param toMillis    End of range (epoch millis, inclusive)
 * @param projects    Map of projectId → (name, colorHex) for enriching project totals.
 *                    Pass an empty map to skip enrichment.
 */
class GetStatisticsUseCase @Inject constructor(
    private val repository: StudyTrackRepository
) {
    operator fun invoke(
        fromMillis: Long,
        toMillis: Long,
        projectNames: Map<Long, Pair<String, String>> = emptyMap()   // id → (name, colorHex)
    ): Flow<Statistics> {
        val dailyFlow = repository.getDailyTotals(fromMillis, toMillis)
        val weeklyFlow = repository.getWeeklyTotals(fromMillis, toMillis)
        val projectFlow = repository.getProjectTotals(fromMillis, toMillis)

        return combine(dailyFlow, weeklyFlow, projectFlow) { daily, weekly, project ->
            Statistics(
                dailyTotals = daily.map { DayTotal(it.date, it.totalMillis) },
                weeklyTotals = weekly.map { WeekTotal(it.date, it.totalMillis) },
                projectTotals = project.map { pt ->
                    val (name, color) = projectNames[pt.projectId] ?: ("No Project" to "#888888")
                    ProjectStudyTotal(
                        projectId = pt.projectId,
                        projectName = name,
                        colorHex = color,
                        totalMillis = pt.totalMillis
                    )
                },
                totalMillisInRange = daily.sumOf { it.totalMillis }
            )
        }
    }
}

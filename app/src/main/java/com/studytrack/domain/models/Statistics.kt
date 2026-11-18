package com.studytrack.domain.models

data class Statistics(
    val dailyTotals: List<DayTotal>,
    val weeklyTotals: List<WeekTotal>,
    val projectTotals: List<ProjectStudyTotal>,
    val totalMillisInRange: Long
)

data class DayTotal(
    val date: String,       // "YYYY-MM-DD"
    val totalMillis: Long
)

data class WeekTotal(
    val week: String,       // "YYYY-WNN"
    val totalMillis: Long
)

data class ProjectStudyTotal(
    val projectId: Long?,
    val projectName: String?,
    val colorHex: String?,
    val totalMillis: Long
)

package com.studytrack.domain.models

data class TimeEntry(
    val id: Long = 0,
    val projectId: Long?,
    val description: String,
    val startTime: Long,
    val endTime: Long?,
    val isRunning: Boolean
) {
    val durationMillis: Long
        get() = if (endTime != null) endTime - startTime else System.currentTimeMillis() - startTime
}

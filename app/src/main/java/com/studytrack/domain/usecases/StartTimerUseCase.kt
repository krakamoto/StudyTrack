package com.studytrack.domain.usecases

import com.studytrack.data.entities.TimeEntryEntity
import com.studytrack.data.repository.StudyTrackRepository
import javax.inject.Inject

/**
 * Starts a new timer.
 * If another timer is already running it is stopped first (safe guard).
 */
class StartTimerUseCase @Inject constructor(
    private val repository: StudyTrackRepository
) {
    suspend operator fun invoke(
        description: String,
        projectId: Long? = null
    ): Long {
        // Stop any currently running entry first
        repository.getCurrentRunningEntryOnce()?.let { running ->
            repository.updateTimeEntry(
                running.copy(
                    endTime = System.currentTimeMillis(),
                    isRunning = false
                )
            )
        }

        val entry = TimeEntryEntity(
            projectId = projectId,
            description = description,
            startTime = System.currentTimeMillis(),
            endTime = null,
            isRunning = true
        )
        return repository.insertTimeEntry(entry)
    }
}

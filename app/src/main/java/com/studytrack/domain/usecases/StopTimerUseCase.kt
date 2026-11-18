package com.studytrack.domain.usecases

import com.studytrack.data.repository.StudyTrackRepository
import javax.inject.Inject

/**
 * Stops the currently running timer, if any.
 * Returns the id of the stopped entry, or null if nothing was running.
 */
class StopTimerUseCase @Inject constructor(
    private val repository: StudyTrackRepository
) {
    suspend operator fun invoke(): Long? {
        val running = repository.getCurrentRunningEntryOnce() ?: return null
        val stopped = running.copy(
            endTime = System.currentTimeMillis(),
            isRunning = false
        )
        repository.updateTimeEntry(stopped)
        return stopped.id
    }
}

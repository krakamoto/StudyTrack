package com.studytrack.domain.usecases

import com.studytrack.data.repository.StudyTrackRepository
import javax.inject.Inject

/**
 * Updates the start and/or end time of an existing time entry.
 * Also allows updating description and projectId.
 */
class UpdateTimeEntryUseCase @Inject constructor(
    private val repository: StudyTrackRepository
) {
    /**
     * @param id          ID of the entry to update.
     * @param startTime   New start time in epoch millis, or null to keep existing.
     * @param endTime     New end time in epoch millis, or null to keep existing
     *                    (pass -1L to explicitly clear endTime / mark as running).
     * @param description New description, or null to keep existing.
     * @param projectId   New projectId, or use a wrapper; null means "no change".
     */
    suspend operator fun invoke(
        id: Long,
        startTime: Long? = null,
        endTime: Long? = null,
        description: String? = null,
        projectId: Long? = null,
        clearEndTime: Boolean = false
    ) {
        val existing = repository.getTimeEntryById(id) ?: return

        val newEndTime = when {
            clearEndTime -> null
            endTime != null -> endTime
            else -> existing.endTime
        }

        val updated = existing.copy(
            startTime = startTime ?: existing.startTime,
            endTime = newEndTime,
            description = description ?: existing.description,
            projectId = projectId ?: existing.projectId,
            isRunning = newEndTime == null
        )
        repository.updateTimeEntry(updated)
    }
}

package com.studytrack.data.backup

import kotlinx.serialization.Serializable

/**
 * Human-readable, versioned backup format. IDs are preserved so project
 * references remain valid after a restore.
 */
@Serializable
data class StudyTrackBackup(
    val format: String = FORMAT,
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long,
    val projects: List<ProjectBackup>,
    val timeEntries: List<TimeEntryBackup>
) {
    companion object {
        const val FORMAT = "studytrack-backup"
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class ProjectBackup(
    val id: Long,
    val name: String,
    val colorHex: String,
    val createdAt: Long
)

@Serializable
data class TimeEntryBackup(
    val id: Long,
    val projectId: Long?,
    val description: String,
    val startTime: Long,
    val endTime: Long?,
    val isRunning: Boolean
)

data class BackupPreview(
    val projectCount: Int,
    val entryCount: Int,
    val exportedAt: Long
)

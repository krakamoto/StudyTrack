package com.studytrack.data.backup

import androidx.room.withTransaction
import com.studytrack.data.db.StudyTrackDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val database: StudyTrackDatabase
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun createBackup(): String = withContext(Dispatchers.IO) {
        val snapshot = StudyTrackBackup(
            exportedAt = System.currentTimeMillis(),
            projects = database.projectDao().getAllProjectsOnce().map { project ->
                ProjectBackup(
                    id = project.id,
                    name = project.name,
                    colorHex = project.colorHex,
                    createdAt = project.createdAt
                )
            },
            timeEntries = database.timeEntryDao().getAllEntriesOnce().map { entry ->
                TimeEntryBackup(
                    id = entry.id,
                    projectId = entry.projectId,
                    description = entry.description,
                    startTime = entry.startTime,
                    endTime = entry.endTime,
                    isRunning = entry.isRunning
                )
            }
        )
        json.encodeToString(snapshot)
    }

    fun parseBackup(content: String): StudyTrackBackup {
        val backup = try {
            json.decodeFromString<StudyTrackBackup>(content)
        } catch (error: Exception) {
            throw IllegalArgumentException("Il file non è un backup StudyTrack valido.", error)
        }
        validate(backup)
        return backup
    }

    suspend fun restoreBackup(backup: StudyTrackBackup) = withContext(Dispatchers.IO) {
        validate(backup)
        database.withTransaction {
            database.timeEntryDao().deleteAll()
            database.projectDao().deleteAll()
            database.projectDao().insertAll(
                backup.projects.map { project ->
                    com.studytrack.data.entities.ProjectEntity(
                        id = project.id,
                        name = project.name,
                        colorHex = project.colorHex,
                        createdAt = project.createdAt
                    )
                }
            )
            database.timeEntryDao().insertAll(
                backup.timeEntries.map { entry ->
                    com.studytrack.data.entities.TimeEntryEntity(
                        id = entry.id,
                        projectId = entry.projectId,
                        description = entry.description,
                        startTime = entry.startTime,
                        endTime = entry.endTime,
                        isRunning = entry.isRunning
                    )
                }
            )
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        database.withTransaction {
            database.timeEntryDao().deleteAll()
            database.projectDao().deleteAll()
        }
    }

    private fun validate(backup: StudyTrackBackup) {
        require(backup.format == StudyTrackBackup.FORMAT) {
            "Formato backup non riconosciuto."
        }
        require(backup.version in 1..StudyTrackBackup.CURRENT_VERSION) {
            "Versione backup non supportata (${backup.version})."
        }
        require(backup.projects.map { it.id }.distinct().size == backup.projects.size) {
            "Il backup contiene progetti con ID duplicati."
        }
        require(backup.timeEntries.map { it.id }.distinct().size == backup.timeEntries.size) {
            "Il backup contiene sessioni con ID duplicati."
        }
        require(backup.projects.all { it.id > 0 && it.name.isNotBlank() && it.colorHex.isNotBlank() }) {
            "Il backup contiene progetti non validi."
        }
        require(backup.timeEntries.all { it.id > 0 && it.startTime > 0 }) {
            "Il backup contiene sessioni non valide."
        }
        require(backup.timeEntries.count { it.isRunning } <= 1) {
            "Il backup contiene più timer attivi contemporaneamente."
        }
        val projectIds = backup.projects.mapTo(hashSetOf()) { it.id }
        require(backup.timeEntries.all { it.projectId == null || it.projectId in projectIds }) {
            "Il backup contiene riferimenti a progetti mancanti."
        }
        require(backup.timeEntries.all { entry ->
            entry.endTime == null || entry.endTime >= entry.startTime
        }) {
            "Il backup contiene sessioni con orari non validi."
        }
    }
}

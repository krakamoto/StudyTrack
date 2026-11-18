package com.studytrack.data.repository

import com.studytrack.data.dao.DailyTotal
import com.studytrack.data.dao.ProjectTotal
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity
import kotlinx.coroutines.flow.Flow

interface StudyTrackRepository {

    // ── Time Entries ──────────────────────────────────────────────────────

    suspend fun insertTimeEntry(entry: TimeEntryEntity): Long
    suspend fun updateTimeEntry(entry: TimeEntryEntity)
    suspend fun deleteTimeEntry(id: Long)
    suspend fun getTimeEntryById(id: Long): TimeEntryEntity?

    fun getCurrentRunningEntry(): Flow<TimeEntryEntity?>
    suspend fun getCurrentRunningEntryOnce(): TimeEntryEntity?

    fun getEntriesByDateRange(fromMillis: Long, toMillis: Long): Flow<List<TimeEntryEntity>>
    fun getAllEntries(): Flow<List<TimeEntryEntity>>

    // ── Statistics ────────────────────────────────────────────────────────

    fun getDailyTotals(fromMillis: Long, toMillis: Long): Flow<List<DailyTotal>>
    fun getWeeklyTotals(fromMillis: Long, toMillis: Long): Flow<List<DailyTotal>>
    fun getProjectTotals(fromMillis: Long, toMillis: Long): Flow<List<ProjectTotal>>

    // ── Projects ──────────────────────────────────────────────────────────

    suspend fun insertProject(project: ProjectEntity): Long
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(id: Long)
    suspend fun getProjectById(id: Long): ProjectEntity?
    fun getAllProjects(): Flow<List<ProjectEntity>>
}

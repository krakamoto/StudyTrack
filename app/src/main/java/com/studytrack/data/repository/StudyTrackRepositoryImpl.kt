package com.studytrack.data.repository

import com.studytrack.data.dao.DailyTotal
import com.studytrack.data.dao.ProjectDao
import com.studytrack.data.dao.ProjectTotal
import com.studytrack.data.dao.TimeEntryDao
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyTrackRepositoryImpl @Inject constructor(
    private val timeEntryDao: TimeEntryDao,
    private val projectDao: ProjectDao
) : StudyTrackRepository {

    // ── Time Entries ──────────────────────────────────────────────────────

    override suspend fun insertTimeEntry(entry: TimeEntryEntity): Long =
        timeEntryDao.insert(entry)

    override suspend fun updateTimeEntry(entry: TimeEntryEntity) =
        timeEntryDao.update(entry)

    override suspend fun deleteTimeEntry(id: Long) =
        timeEntryDao.deleteById(id)

    override suspend fun getTimeEntryById(id: Long): TimeEntryEntity? =
        timeEntryDao.getById(id)

    override fun getCurrentRunningEntry(): Flow<TimeEntryEntity?> =
        timeEntryDao.getCurrentRunningEntry()

    override suspend fun getCurrentRunningEntryOnce(): TimeEntryEntity? =
        timeEntryDao.getCurrentRunningEntryOnce()

    override fun getEntriesByDateRange(fromMillis: Long, toMillis: Long): Flow<List<TimeEntryEntity>> =
        timeEntryDao.getEntriesByDateRange(fromMillis, toMillis)

    override fun getAllEntries(): Flow<List<TimeEntryEntity>> =
        timeEntryDao.getAllEntries()

    // ── Statistics ────────────────────────────────────────────────────────

    override fun getDailyTotals(fromMillis: Long, toMillis: Long): Flow<List<DailyTotal>> =
        timeEntryDao.getDailyTotals(fromMillis, toMillis)

    override fun getWeeklyTotals(fromMillis: Long, toMillis: Long): Flow<List<DailyTotal>> =
        timeEntryDao.getWeeklyTotals(fromMillis, toMillis)

    override fun getProjectTotals(fromMillis: Long, toMillis: Long): Flow<List<ProjectTotal>> =
        timeEntryDao.getProjectTotals(fromMillis, toMillis)

    // ── Projects ──────────────────────────────────────────────────────────

    override suspend fun insertProject(project: ProjectEntity): Long =
        projectDao.insert(project)

    override suspend fun updateProject(project: ProjectEntity) =
        projectDao.update(project)

    override suspend fun deleteProject(id: Long) =
        projectDao.deleteById(id)

    override suspend fun getProjectById(id: Long): ProjectEntity? =
        projectDao.getById(id)

    override fun getAllProjects(): Flow<List<ProjectEntity>> =
        projectDao.getAllProjects()
}

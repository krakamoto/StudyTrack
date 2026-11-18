package com.studytrack.data.dao

import androidx.room.*
import com.studytrack.data.entities.TimeEntryEntity
import kotlinx.coroutines.flow.Flow

data class DailyTotal(
    val date: String,          // "YYYY-MM-DD"
    val totalMillis: Long
)

data class ProjectTotal(
    val projectId: Long?,
    val totalMillis: Long
)

@Dao
interface TimeEntryDao {

    // ── Insert / Update / Delete ──────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimeEntryEntity): Long

    @Update
    suspend fun update(entry: TimeEntryEntity)

    @Delete
    suspend fun delete(entry: TimeEntryEntity)

    @Query("DELETE FROM time_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ── Single-entry queries ──────────────────────────────────────────────

    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getById(id: Long): TimeEntryEntity?

    /** Returns the currently running entry, or null. */
    @Query("SELECT * FROM time_entries WHERE isRunning = 1 LIMIT 1")
    fun getCurrentRunningEntry(): Flow<TimeEntryEntity?>

    /** One-shot version for use-cases that need a suspend check. */
    @Query("SELECT * FROM time_entries WHERE isRunning = 1 LIMIT 1")
    suspend fun getCurrentRunningEntryOnce(): TimeEntryEntity?

    // ── Range queries ─────────────────────────────────────────────────────

    /**
     * Returns all entries whose startTime falls within [fromMillis, toMillis].
     * Ordered newest first.
     */
    @Query(
        """
        SELECT * FROM time_entries
        WHERE startTime >= :fromMillis AND startTime <= :toMillis
        ORDER BY startTime DESC
        """
    )
    fun getEntriesByDateRange(fromMillis: Long, toMillis: Long): Flow<List<TimeEntryEntity>>

    // ── Aggregate queries ─────────────────────────────────────────────────

    /**
     * Daily totals (sum of duration) for entries in [fromMillis, toMillis].
     * Skips running entries (endTime IS NULL).
     * Returns rows ordered by date ascending.
     */
    @Query(
        """
        SELECT
            strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime') AS date,
            SUM(endTime - startTime) AS totalMillis
        FROM time_entries
        WHERE isRunning = 0
          AND endTime IS NOT NULL
          AND startTime >= :fromMillis
          AND startTime <= :toMillis
        GROUP BY date
        ORDER BY date ASC
        """
    )
    fun getDailyTotals(fromMillis: Long, toMillis: Long): Flow<List<DailyTotal>>

    /**
     * Weekly totals (ISO week, year). Same logic as daily totals.
     */
    @Query(
        """
        SELECT
            strftime('%Y-W%W', startTime / 1000, 'unixepoch', 'localtime') AS date,
            SUM(endTime - startTime) AS totalMillis
        FROM time_entries
        WHERE isRunning = 0
          AND endTime IS NOT NULL
          AND startTime >= :fromMillis
          AND startTime <= :toMillis
        GROUP BY date
        ORDER BY date ASC
        """
    )
    fun getWeeklyTotals(fromMillis: Long, toMillis: Long): Flow<List<DailyTotal>>

    /**
     * Total time per project within [fromMillis, toMillis].
     * projectId = null means no project assigned.
     */
    @Query(
        """
        SELECT
            projectId,
            SUM(endTime - startTime) AS totalMillis
        FROM time_entries
        WHERE isRunning = 0
          AND endTime IS NOT NULL
          AND startTime >= :fromMillis
          AND startTime <= :toMillis
        GROUP BY projectId
        ORDER BY totalMillis DESC
        """
    )
    fun getProjectTotals(fromMillis: Long, toMillis: Long): Flow<List<ProjectTotal>>

    /** All entries, newest first (for a full history list). */
    @Query("SELECT * FROM time_entries ORDER BY startTime DESC")
    fun getAllEntries(): Flow<List<TimeEntryEntity>>

    /** One-shot snapshot used by backup/export. */
    @Query("SELECT * FROM time_entries ORDER BY startTime DESC")
    suspend fun getAllEntriesOnce(): List<TimeEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimeEntryEntity>)

    @Query("DELETE FROM time_entries")
    suspend fun deleteAll()
}

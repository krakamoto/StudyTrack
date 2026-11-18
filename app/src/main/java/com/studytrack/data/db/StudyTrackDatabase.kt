package com.studytrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.studytrack.data.dao.ProjectDao
import com.studytrack.data.dao.TimeEntryDao
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity

@Database(
    entities = [TimeEntryEntity::class, ProjectEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyTrackDatabase : RoomDatabase() {
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun projectDao(): ProjectDao

    companion object {
        const val DATABASE_NAME = "studytrack.db"
    }
}

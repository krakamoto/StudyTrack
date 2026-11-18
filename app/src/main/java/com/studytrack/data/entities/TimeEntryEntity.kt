package com.studytrack.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("projectId"),
        Index("startTime"),
        Index("isRunning")
    ]
)
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long?,
    val description: String,
    val startTime: Long,       // epoch millis
    val endTime: Long?,        // null if still running
    val isRunning: Boolean
)

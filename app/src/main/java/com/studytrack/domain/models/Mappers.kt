package com.studytrack.domain.models

import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity

// ── TimeEntry mappers ─────────────────────────────────────────────────────────

fun TimeEntryEntity.toDomain() = TimeEntry(
    id = id,
    projectId = projectId,
    description = description,
    startTime = startTime,
    endTime = endTime,
    isRunning = isRunning
)

fun TimeEntry.toEntity() = TimeEntryEntity(
    id = id,
    projectId = projectId,
    description = description,
    startTime = startTime,
    endTime = endTime,
    isRunning = isRunning
)

// ── Project mappers ───────────────────────────────────────────────────────────

fun ProjectEntity.toDomain() = Project(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt
)

fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt
)

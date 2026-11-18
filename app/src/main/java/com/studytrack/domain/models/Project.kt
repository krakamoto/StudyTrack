package com.studytrack.domain.models

data class Project(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val createdAt: Long
)

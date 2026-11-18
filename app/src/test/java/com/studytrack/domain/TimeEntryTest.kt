package com.studytrack.domain

import com.studytrack.domain.models.TimeEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeEntryTest {

    @Test
    fun `completed entry duration is end minus start`() {
        val entry = TimeEntry(
            id = 1,
            projectId = null,
            description = "Algorithms",
            startTime = 1_000L,
            endTime = 91_000L,
            isRunning = false
        )

        assertEquals(90_000L, entry.durationMillis)
    }
}

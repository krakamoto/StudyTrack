package com.studytrack.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {

    @Test
    fun `formats seconds when under one minute`() {
        assertEquals("42s", formatDuration(42_000L))
    }

    @Test
    fun `formats minutes and seconds when under one hour`() {
        assertEquals("5m 07s", formatDuration(307_000L))
    }

    @Test
    fun `formats hours and minutes when over one hour`() {
        assertEquals("2h 05m", formatDuration(7_500_000L))
    }
}

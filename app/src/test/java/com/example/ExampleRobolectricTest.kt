package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HourBlockEntity
import com.example.domain.model.DefaultExecutiveTemplate
import com.example.domain.model.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies DayLedger app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DayLedger", appName)
    }

    @Test
    fun `empty 24-hour slots start with no pre-filled tasks`() {
        val emptySlots = DefaultExecutiveTemplate.generateEmpty24HourSlots("2026-10-11")
        assertEquals(24, emptySlots.size)
        // All titles must be empty so user adds tasks themselves
        assertTrue(emptySlots.all { it.title.isEmpty() })
        assertEquals(0, emptySlots.first().hour)
        assertEquals(23, emptySlots.last().hour)

        val validation = ValidationResult.validateTimeline(emptySlots)
        assertTrue(validation.isValid)
    }

    @Test
    fun `validation catches overlapping tasks with 30-minute precision`() {
        val tasks = listOf(
            HourBlockEntity(
                date = "2026-10-11",
                hour = 8,
                minute = 30,
                durationMinutes = 90, // 08:30 - 10:00
                title = "Strategy Block"
            ),
            HourBlockEntity(
                date = "2026-10-11",
                hour = 9,
                minute = 30,
                durationMinutes = 30, // 09:30 - 10:00 (overlaps!)
                title = "Operations Sync"
            )
        )

        val validation = ValidationResult.validateTimeline(tasks)
        assertFalse(validation.isValid)
        assertTrue(validation.conflictingHours.contains(8))
        assertTrue(validation.conflictingHours.contains(9))
    }
}

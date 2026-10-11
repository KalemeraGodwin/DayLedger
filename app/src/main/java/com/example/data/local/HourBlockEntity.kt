package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hour_blocks",
    indices = [
        Index(value = ["date"]),
        Index(value = ["date", "hour"])
    ]
)
data class HourBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: yyyy-MM-dd
    val hour: Int = 0, // 0 to 23 (start hour)
    val minute: Int = 0, // 0 or 30 (30-minute increments)
    val durationMinutes: Int = 60, // 30, 60, 90, 120, 150, etc.
    val title: String = "", // empty string means available / unscheduled slot
    val category: String = "General",
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val durationHours: Int = 1,
    val isCompleted: Boolean = false,
    val reviewLog: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val startMinuteOfDay: Int
        get() = hour * 60 + minute

    val endMinuteOfDay: Int
        get() = startMinuteOfDay + durationMinutes

    fun formatStartTime(): String = String.format("%02d:%02d", hour, minute)

    fun formatEndTime(): String {
        val endMin = endMinuteOfDay
        val endH = (endMin / 60).coerceAtMost(24)
        val endM = endMin % 60
        return String.format("%02d:%02d", endH, endM)
    }

    fun formatDuration(): String {
        val h = durationMinutes / 60
        val m = durationMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "$h hr${if (h > 1) "s" else ""}"
            else -> "$m min"
        }
    }
}

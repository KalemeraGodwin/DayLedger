package com.example.domain.model

import com.example.data.local.HourBlockEntity

data class ValidationResult(
    val isValid: Boolean,
    val warnings: List<String> = emptyList(),
    val conflictingHours: Set<Int> = emptySet(),
    val totalScheduledHours: Int = 24
) {
    companion object {
        fun validateTimeline(blocks: List<HourBlockEntity>): ValidationResult {
            val warnings = mutableListOf<String>()
            val conflictingHours = mutableSetOf<Int>()

            val scheduledTasks = blocks.filter { it.title.isNotBlank() }
                .sortedBy { it.startMinuteOfDay }

            if (scheduledTasks.isEmpty()) {
                return ValidationResult(isValid = true, totalScheduledHours = 24)
            }

            var totalScheduledMinutes = 0

            for (i in scheduledTasks.indices) {
                val current = scheduledTasks[i]
                val start = current.startMinuteOfDay
                val duration = current.durationMinutes.coerceAtLeast(30)
                val end = start + duration
                totalScheduledMinutes += duration

                // Check clipping 24-hour cycle (1440 minutes)
                if (end > 1440) {
                    conflictingHours.add(current.hour)
                    warnings.add(
                        "Task '${current.title}' (${current.formatStartTime()}) extends until ${current.formatEndTime()}, clipping past midnight (24:00)."
                    )
                }

                // Check overlaps with subsequent scheduled tasks
                for (j in (i + 1) until scheduledTasks.size) {
                    val next = scheduledTasks[j]
                    if (next.startMinuteOfDay < end) {
                        conflictingHours.add(current.hour)
                        conflictingHours.add(next.hour)
                        warnings.add(
                            "Collision: '${current.title}' (${current.formatStartTime()}–${current.formatEndTime()}) overlaps with '${next.title}' (${next.formatStartTime()})."
                        )
                    }
                }
            }

            if (totalScheduledMinutes > 1440) {
                val totalH = totalScheduledMinutes / 60
                val totalM = totalScheduledMinutes % 60
                warnings.add("Total scheduled task time (${totalH}h ${totalM}m) exceeds the 24-hour limit.")
            }

            return ValidationResult(
                isValid = warnings.isEmpty(),
                warnings = warnings,
                conflictingHours = conflictingHours,
                totalScheduledHours = (totalScheduledMinutes + 59) / 60
            )
        }
    }
}

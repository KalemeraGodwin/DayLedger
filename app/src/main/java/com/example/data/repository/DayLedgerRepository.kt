package com.example.data.repository

import com.example.data.local.DayLedgerDao
import com.example.data.local.DayPlanEntity
import com.example.data.local.HourBlockEntity
import com.example.domain.model.CategoryAccomplishment
import com.example.domain.model.DailyPerformancePoint
import com.example.domain.model.DefaultExecutiveTemplate
import com.example.domain.model.PerformanceReport
import com.example.domain.model.SkippedTaskSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DayLedgerRepository(private val dao: DayLedgerDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val shortLabelFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.US)
    private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.US)

    fun getHourBlocks(date: String): Flow<List<HourBlockEntity>> {
        return dao.getHourBlocksForDate(date)
    }

    fun getDayPlan(date: String): Flow<DayPlanEntity?> {
        return dao.getDayPlan(date)
    }

    suspend fun ensureDayInitialized(date: String) {
        val today = LocalDate.now().format(dateFormatter)
        val isPastDate = isDateInPast(date, today)

        val existingPlan = dao.getDayPlanOnce(date)
        if (existingPlan == null) {
            val (quote, author) = DefaultExecutiveTemplate.getDailyQuoteForDate(date)
            dao.insertOrUpdateDayPlan(
                DayPlanEntity(
                    date = date,
                    quoteText = quote,
                    quoteAuthor = author,
                    isQuoteLocked = isPastDate
                )
            )
        } else if (isPastDate && !existingPlan.isQuoteLocked) {
            dao.insertOrUpdateDayPlan(existingPlan.copy(isQuoteLocked = true))
        }

        val existingBlocks = dao.getHourBlocksForDateOnce(date)
        if (existingBlocks.isEmpty()) {
            val emptySlots = DefaultExecutiveTemplate.generateEmpty24HourSlots(date)
            dao.insertHourBlocks(emptySlots)
        }
    }

    suspend fun updateCompletion(id: Long, isCompleted: Boolean) {
        dao.updateCompletionStatus(id, isCompleted)
    }

    suspend fun updateReviewLog(id: Long, reviewLog: String) {
        dao.updateReviewLog(id, reviewLog)
    }

    /**
     * Saves or updates a task with automatic chronological cascading with 30-minute granularity.
     * "Make it automatic that if i add hours to a task, the next task begins at the exact time the other ended."
     */
    suspend fun saveOrUpdateHourBlockWithCascade(
        date: String,
        updatedBlock: HourBlockEntity,
        autoChainNext: Boolean = true
    ) {
        val existingBlocks = dao.getHourBlocksForDateOnce(date).toMutableList()

        val startMin = updatedBlock.startMinuteOfDay
        val oldBlock = existingBlocks.firstOrNull {
            (updatedBlock.id != 0L && it.id == updatedBlock.id) ||
            (it.startMinuteOfDay == startMin && it.date == date)
        }

        val oldDuration = oldBlock?.durationMinutes ?: 60
        val newDuration = updatedBlock.durationMinutes.coerceAtLeast(30)
        val oldEndMin = startMin + oldDuration
        val newEndMin = startMin + newDuration
        val addedMinutes = newDuration - oldDuration

        // Extract all other scheduled tasks (tasks with non-empty title)
        val otherScheduledTasks = existingBlocks.filter {
            it.id != updatedBlock.id && it.title.isNotBlank() && it.startMinuteOfDay != startMin
        }.sortedBy { it.startMinuteOfDay }.toMutableList()

        // Cascade subsequent tasks if minutes were added or auto-chaining is active
        if (addedMinutes > 0 || autoChainNext) {
            var currentChainEnd = newEndMin

            for (i in otherScheduledTasks.indices) {
                val task = otherScheduledTasks[i]
                if (task.startMinuteOfDay >= startMin) {
                    val wasAdjacent = (task.startMinuteOfDay == oldEndMin)
                    val isOverlapped = (task.startMinuteOfDay < currentChainEnd)

                    if (wasAdjacent || isOverlapped) {
                        val shiftedStart = currentChainEnd
                        val shiftedH = shiftedStart / 60
                        val shiftedM = shiftedStart % 60
                        val shiftedTask = task.copy(
                            hour = shiftedH,
                            minute = shiftedM,
                            updatedAt = System.currentTimeMillis()
                        )
                        otherScheduledTasks[i] = shiftedTask
                        currentChainEnd = shiftedStart + shiftedTask.durationMinutes.coerceAtLeast(30)
                    }
                }
            }
        }

        // Combine all scheduled tasks
        val allScheduledTasks = (otherScheduledTasks + updatedBlock.copy(
            hour = updatedBlock.startMinuteOfDay / 60,
            minute = updatedBlock.startMinuteOfDay % 60,
            durationMinutes = newDuration,
            durationHours = (newDuration + 59) / 60,
            updatedAt = System.currentTimeMillis()
        )).filter { it.title.isNotBlank() }
          .sortedBy { it.startMinuteOfDay }

        // Preserve existing review logs
        val existingLogs = existingBlocks.associate { it.startMinuteOfDay to it.reviewLog }

        // Reconstruct continuous 24-hour feed (0 to 1440 minutes)
        val reconstructedFeed = mutableListOf<HourBlockEntity>()
        var cursorMin = 0

        while (cursorMin < 1440) {
            val taskAtCursor = allScheduledTasks.firstOrNull { it.startMinuteOfDay == cursorMin }
            if (taskAtCursor != null) {
                reconstructedFeed.add(taskAtCursor)
                cursorMin += taskAtCursor.durationMinutes.coerceAtLeast(30)
            } else {
                val nextTask = allScheduledTasks.firstOrNull { it.startMinuteOfDay > cursorMin }
                val nextStopMin = nextTask?.startMinuteOfDay ?: 1440

                val stepMin = if (cursorMin % 60 != 0) {
                    minOf(60 - (cursorMin % 60), nextStopMin - cursorMin)
                } else {
                    minOf(60, nextStopMin - cursorMin)
                }

                val slotH = cursorMin / 60
                val slotM = cursorMin % 60
                val log = existingLogs[cursorMin] ?: ""

                reconstructedFeed.add(
                    HourBlockEntity(
                        date = date,
                        hour = slotH,
                        minute = slotM,
                        durationMinutes = stepMin,
                        durationHours = (stepMin + 59) / 60,
                        title = "",
                        category = "General",
                        priority = "MEDIUM",
                        isCompleted = false,
                        reviewLog = log
                    )
                )
                cursorMin += stepMin
            }
        }

        // Tasks pushed past midnight (24:00)
        val beyondMidnight = allScheduledTasks.filter { it.startMinuteOfDay >= 1440 }
        reconstructedFeed.addAll(beyondMidnight)

        dao.replaceBlocksForDate(date, reconstructedFeed)
    }

    suspend fun clearTaskFromSlot(date: String, block: HourBlockEntity) {
        val existingBlocks = dao.getHourBlocksForDateOnce(date)
        val remainingTasks = existingBlocks.filter {
            it.id != block.id && it.startMinuteOfDay != block.startMinuteOfDay && it.title.isNotBlank()
        }.sortedBy { it.startMinuteOfDay }

        val existingLogs = existingBlocks.associate { it.startMinuteOfDay to it.reviewLog }

        val reconstructedFeed = mutableListOf<HourBlockEntity>()
        var cursorMin = 0

        while (cursorMin < 1440) {
            val taskAtCursor = remainingTasks.firstOrNull { it.startMinuteOfDay == cursorMin }
            if (taskAtCursor != null) {
                reconstructedFeed.add(taskAtCursor)
                cursorMin += taskAtCursor.durationMinutes.coerceAtLeast(30)
            } else {
                val nextTask = remainingTasks.firstOrNull { it.startMinuteOfDay > cursorMin }
                val nextStopMin = nextTask?.startMinuteOfDay ?: 1440

                val stepMin = if (cursorMin % 60 != 0) {
                    minOf(60 - (cursorMin % 60), nextStopMin - cursorMin)
                } else {
                    minOf(60, nextStopMin - cursorMin)
                }

                val slotH = cursorMin / 60
                val slotM = cursorMin % 60
                val log = existingLogs[cursorMin] ?: if (cursorMin == block.startMinuteOfDay) block.reviewLog else ""

                reconstructedFeed.add(
                    HourBlockEntity(
                        date = date,
                        hour = slotH,
                        minute = slotM,
                        durationMinutes = stepMin,
                        durationHours = (stepMin + 59) / 60,
                        title = "",
                        category = "General",
                        priority = "MEDIUM",
                        isCompleted = false,
                        reviewLog = log
                    )
                )
                cursorMin += stepMin
            }
        }

        val beyondMidnight = remainingTasks.filter { it.startMinuteOfDay >= 1440 }
        reconstructedFeed.addAll(beyondMidnight)

        dao.replaceBlocksForDate(date, reconstructedFeed)
    }

    suspend fun clearAllTasksForDate(date: String) {
        val emptySlots = DefaultExecutiveTemplate.generateEmpty24HourSlots(date)
        dao.replaceBlocksForDate(date, emptySlots)
    }

    suspend fun loadExecutiveTemplateForDate(date: String) {
        val templateBlocks = DefaultExecutiveTemplate.generateExecutiveRoutineTemplate(date)
        dao.replaceBlocksForDate(date, templateBlocks)
    }

    suspend fun updateDayQuote(date: String, quoteText: String, quoteAuthor: String) {
        val existingPlan = dao.getDayPlanOnce(date)
        val today = LocalDate.now().format(dateFormatter)
        val isPast = isDateInPast(date, today)

        if (isPast) {
            return
        }

        val updated = existingPlan?.copy(
            quoteText = quoteText,
            quoteAuthor = quoteAuthor,
            lastModified = System.currentTimeMillis()
        ) ?: DayPlanEntity(
            date = date,
            quoteText = quoteText,
            quoteAuthor = quoteAuthor,
            isQuoteLocked = false
        )
        dao.insertOrUpdateDayPlan(updated)
    }

    fun getPerformanceReport(days: Int = 30): Flow<PerformanceReport> {
        val endDateObj = LocalDate.now()
        val startDateObj = endDateObj.minusDays((days - 1).toLong())
        val startDateStr = startDateObj.format(dateFormatter)
        val endDateStr = endDateObj.format(dateFormatter)

        return dao.getHourBlocksInRange(startDateStr, endDateStr).map { blocks ->
            compileReport(blocks, startDateObj, endDateObj, days)
        }
    }

    private fun compileReport(
        blocks: List<HourBlockEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        daysCount: Int
    ): PerformanceReport {
        val scheduledTasks = blocks.filter { it.title.isNotBlank() }

        if (scheduledTasks.isEmpty()) {
            return PerformanceReport(timelineDays = daysCount)
        }

        val totalScheduled = scheduledTasks.size
        val totalCompleted = scheduledTasks.count { it.isCompleted }
        val overallRate = if (totalScheduled > 0) {
            (totalCompleted.toFloat() / totalScheduled.toFloat()) * 100f
        } else 0f

        val categoryStats = scheduledTasks.groupBy { it.category }
            .map { (cat, list) ->
                val comp = list.count { it.isCompleted }
                val tot = list.size
                val pct = if (tot > 0) (comp.toFloat() / tot) * 100f else 0f
                CategoryAccomplishment(
                    category = cat,
                    completedCount = comp,
                    totalCount = tot,
                    completionPercentage = pct
                )
            }
            .sortedByDescending { it.completedCount }
            .take(3)

        val skippedStats = scheduledTasks.filter { !it.isCompleted }
            .groupBy { it.title.trim() }
            .map { (title, list) ->
                val category = list.firstOrNull()?.category ?: "General"
                val skippedCount = list.size
                val totalForTitle = scheduledTasks.count { it.title.trim() == title }
                val skipRate = if (totalForTitle > 0) (skippedCount.toFloat() / totalForTitle) * 100f else 100f
                SkippedTaskSummary(
                    title = title,
                    category = category,
                    skippedCount = skippedCount,
                    totalScheduled = totalForTitle,
                    skipRatePercentage = skipRate
                )
            }
            .sortedByDescending { it.skippedCount }
            .take(5)

        val tasksByDate = scheduledTasks.groupBy { it.date }
        val dailyPoints = mutableListOf<DailyPerformancePoint>()
        var bestRate = 0f

        var cursor = startDate
        while (!cursor.isAfter(endDate)) {
            val dateStr = cursor.format(dateFormatter)
            val dayTasks = tasksByDate[dateStr] ?: emptyList()
            val dayCompleted = dayTasks.count { it.isCompleted }
            val dayTotal = dayTasks.size
            val dayRate = if (dayTotal > 0) (dayCompleted.toFloat() / dayTotal) * 100f else 0f
            if (dayRate > bestRate) {
                bestRate = dayRate
            }

            dailyPoints.add(
                DailyPerformancePoint(
                    date = dateStr,
                    shortLabel = cursor.format(shortLabelFormatter),
                    dayOfWeek = cursor.format(dayOfWeekFormatter),
                    completedCount = dayCompleted,
                    totalCount = dayTotal,
                    completionRate = dayRate
                )
            )
            cursor = cursor.plusDays(1)
        }

        var streak = 0
        for (i in dailyPoints.indices.reversed()) {
            val point = dailyPoints[i]
            if (point.totalCount > 0 && point.completionRate >= 40f) {
                streak++
            } else if (point.totalCount > 0) {
                break
            }
        }

        return PerformanceReport(
            timelineDays = daysCount,
            overallCompletionRate = overallRate,
            totalTasksScheduled = totalScheduled,
            totalTasksCompleted = totalCompleted,
            topAccomplishedTaskTypes = categoryStats,
            mostFrequentlySkippedTasks = skippedStats,
            dailyTrend = dailyPoints,
            currentStreak = streak,
            bestDayCompletionRate = bestRate
        )
    }

    private fun isDateInPast(dateStr: String, todayStr: String): Boolean {
        return try {
            val date = LocalDate.parse(dateStr, dateFormatter)
            val today = LocalDate.parse(todayStr, dateFormatter)
            date.isBefore(today)
        } catch (e: Exception) {
            false
        }
    }
}

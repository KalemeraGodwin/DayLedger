package com.example.domain.model

data class PerformanceReport(
    val timelineDays: Int = 30,
    val overallCompletionRate: Float = 0f, // 0..100
    val totalTasksScheduled: Int = 0,
    val totalTasksCompleted: Int = 0,
    val topAccomplishedTaskTypes: List<CategoryAccomplishment> = emptyList(),
    val mostFrequentlySkippedTasks: List<SkippedTaskSummary> = emptyList(),
    val dailyTrend: List<DailyPerformancePoint> = emptyList(),
    val currentStreak: Int = 0,
    val bestDayCompletionRate: Float = 0f
)

data class CategoryAccomplishment(
    val category: String,
    val completedCount: Int,
    val totalCount: Int,
    val completionPercentage: Float
)

data class SkippedTaskSummary(
    val title: String,
    val category: String,
    val skippedCount: Int,
    val totalScheduled: Int,
    val skipRatePercentage: Float
)

data class DailyPerformancePoint(
    val date: String, // yyyy-MM-dd
    val shortLabel: String, // e.g. "10/01"
    val dayOfWeek: String, // e.g. "Mon"
    val completedCount: Int,
    val totalCount: Int,
    val completionRate: Float // 0..100
)

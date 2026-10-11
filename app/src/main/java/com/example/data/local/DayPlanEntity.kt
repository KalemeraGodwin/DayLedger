package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_plans")
data class DayPlanEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val quoteText: String,
    val quoteAuthor: String,
    val isQuoteLocked: Boolean = false,
    val dayNotes: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

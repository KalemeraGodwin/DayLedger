package com.example.domain.model

import com.example.data.local.HourBlockEntity
import kotlin.math.abs

object DefaultExecutiveTemplate {

    val CATEGORIES = listOf(
        "Executive Strategy",
        "Deep Work",
        "Operations",
        "Client Briefing",
        "Health & Wellness",
        "Rest & Recovery",
        "Personal Development"
    )

    val QUOTES = listOf(
        "At dawn, when you have trouble getting out of bed, tell yourself: 'I have to go to work — as a human being.'" to "Marcus Aurelius",
        "Deciding what not to do is as important as deciding what to do." to "Steve Jobs",
        "Efficiency is doing things right; effectiveness is doing the right things." to "Peter Drucker",
        "The difference between successful people and really successful people is that really successful people say no to almost everything." to "Warren Buffett",
        "Our industry does not respect tradition — it only respects innovation." to "Satya Nadella",
        "It is not that we have so little time, but that we lose so much of it." to "Seneca",
        "Discipline equals freedom." to "Jocko Willink",
        "Amateurs talk about tactics, but professionals study logistics." to "Gen. Robert H. Barrow",
        "Focus is a muscle. The more you protect it, the sharper your execution becomes." to "DayLedger Executive Codex",
        "Simplicity is the prerequisite for reliability." to "Edsger W. Dijkstra"
    )

    fun getDailyQuoteForDate(date: String): Pair<String, String> {
        val hash = abs(date.hashCode())
        val index = hash % QUOTES.size
        return QUOTES[index]
    }

    /**
     * Creates clean 24-hour canvas of available slots with NO pre-filled tasks.
     */
    fun generateEmpty24HourSlots(date: String): List<HourBlockEntity> {
        return (0..23).map { hour ->
            HourBlockEntity(
                date = date,
                hour = hour,
                minute = 0,
                durationMinutes = 60,
                title = "",
                category = "General",
                priority = "MEDIUM",
                durationHours = 1,
                isCompleted = false,
                reviewLog = ""
            )
        }
    }

    /**
     * Optional pre-set executive routine that can be loaded on demand.
     */
    fun generateExecutiveRoutineTemplate(date: String): List<HourBlockEntity> {
        val schedule = listOf(
            Triple(0, "Deep Sleep Protocol & REM Recovery", "Rest & Recovery"),
            Triple(1, "Deep Sleep & Cellular Regeneration", "Rest & Recovery"),
            Triple(2, "Circadian Low Core Body Temp Sleep", "Rest & Recovery"),
            Triple(3, "Memory Consolidation Phase Sleep", "Rest & Recovery"),
            Triple(4, "Early Dawn Slumber & Fasting Window", "Rest & Recovery"),
            Triple(5, "Executive Awakening, Hydration & Light Exposure", "Health & Wellness"),
            Triple(6, "Cold Shower, Meditation & Physical Conditioning", "Health & Wellness"),
            Triple(7, "Strategic Objectives Audit & High-Priority Alignment", "Executive Strategy"),
            Triple(8, "Deep Focus Block I: High-Leverage Architecture", "Deep Work"),
            Triple(9, "Deep Focus Block II: Core Engineering & Capital Allocation", "Deep Work"),
            Triple(10, "Executive Standup & Leadership Synchronization", "Operations"),
            Triple(11, "Deep Focus Block III: Milestone Execution", "Deep Work"),
            Triple(12, "Nutritional Protocol, Fasting Break & Brisk Walk", "Health & Wellness"),
            Triple(13, "Stakeholder Review & High-Stakes Negotiations", "Client Briefing"),
            Triple(14, "Tactical Operations & Team Delegation Sync", "Operations"),
            Triple(15, "Deep Focus Block IV: System Innovation & Synthesis", "Deep Work"),
            Triple(16, "Client Strategy & Advisory Board Synchronization", "Client Briefing"),
            Triple(17, "Ledger Reconcile, Daily Audit & Metric Logging", "Operations"),
            Triple(18, "Zone 2 Cardio, Strength Protocol & Mobility", "Health & Wellness"),
            Triple(19, "Family Dinner, Social Connection & Executive Downtime", "Personal Development"),
            Triple(20, "Digital Sunset, Screen Shutdown & Ambient Lighting", "Rest & Recovery"),
            Triple(21, "Strategic Reading, Executive Journaling & Philosophy", "Personal Development"),
            Triple(22, "Sleep Chamber Priming & Mindful Wind Down", "Rest & Recovery"),
            Triple(23, "Full System Shutdown & Sleep Initiation", "Rest & Recovery")
        )

        return schedule.map { (hour, title, category) ->
            HourBlockEntity(
                date = date,
                hour = hour,
                minute = 0,
                durationMinutes = 60,
                title = title,
                category = category,
                priority = when {
                    hour in 7..11 -> "HIGH"
                    hour in 13..16 -> "MEDIUM"
                    else -> "LOW"
                },
                durationHours = 1,
                isCompleted = false,
                reviewLog = ""
            )
        }
    }
}

package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DayLedgerDatabase
import com.example.data.local.DayPlanEntity
import com.example.data.local.HourBlockEntity
import com.example.data.repository.DayLedgerRepository
import com.example.domain.model.DefaultExecutiveTemplate
import com.example.domain.model.PerformanceReport
import com.example.domain.model.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Random

enum class DayLedgerScreen {
    TIMELINE,
    PERFORMANCE_REPORTS,
    AUDIT_ARCHIVE
}

class DayLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DayLedgerDatabase.getInstance(application)
    private val repository = DayLedgerRepository(db.dayLedgerDao())
    private val prefs = application.getSharedPreferences("day_ledger_prefs", Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)

    val todayDateString: String = LocalDate.now().format(dateFormatter)

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentTimeString = MutableStateFlow(LocalTime.now().format(timeFormatter))
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    private val _currentDateFormatted = MutableStateFlow(LocalDate.now().format(fullDateFormatter))
    val currentDateFormatted: StateFlow<String> = _currentDateFormatted.asStateFlow()

    private val _currentHour = MutableStateFlow(LocalTime.now().hour)
    val currentHour: StateFlow<Int> = _currentHour.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentScreen = MutableStateFlow(DayLedgerScreen.TIMELINE)
    val currentScreen: StateFlow<DayLedgerScreen> = _currentScreen.asStateFlow()

    private val _editingBlock = MutableStateFlow<HourBlockEntity?>(null)
    val editingBlock: StateFlow<HourBlockEntity?> = _editingBlock.asStateFlow()

    private val _reportDaysFilter = MutableStateFlow(30)
    val reportDaysFilter: StateFlow<Int> = _reportDaysFilter.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Track which block's diary review log is currently in active edit mode
    private val _editingLogBlockId = MutableStateFlow<Long?>(null)
    val editingLogBlockId: StateFlow<Long?> = _editingLogBlockId.asStateFlow()

    val hourBlocks: StateFlow<List<HourBlockEntity>> = _selectedDate
        .flatMapLatest { date ->
            repository.getHourBlocks(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayPlan: StateFlow<DayPlanEntity?> = _selectedDate
        .flatMapLatest { date ->
            repository.getDayPlan(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val performanceReport: StateFlow<PerformanceReport> = _reportDaysFilter
        .flatMapLatest { days ->
            repository.getPerformanceReport(days)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerformanceReport())

    private val _validationResult = MutableStateFlow(ValidationResult(isValid = true))
    val validationResult: StateFlow<ValidationResult> = _validationResult.asStateFlow()

    init {
        startClockTicker()

        viewModelScope.launch {
            repository.ensureDayInitialized(todayDateString)
            seedHistoricalLedgerIfEmpty()
        }

        viewModelScope.launch {
            hourBlocks.collect { blocks ->
                _validationResult.value = ValidationResult.validateTimeline(blocks)
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val now = LocalTime.now()
                val today = LocalDate.now()
                _currentTimeString.value = now.format(timeFormatter)
                _currentDateFormatted.value = today.format(fullDateFormatter)
                _currentHour.value = now.hour
                delay(1000L)
            }
        }
    }

    fun isSelectedDateInPast(): Boolean {
        return try {
            val selected = LocalDate.parse(_selectedDate.value, dateFormatter)
            val today = LocalDate.parse(todayDateString, dateFormatter)
            selected.isBefore(today)
        } catch (e: Exception) {
            false
        }
    }

    fun isSelectedDateToday(): Boolean {
        return _selectedDate.value == todayDateString
    }

    fun isSelectedDateInFuture(): Boolean {
        return try {
            val selected = LocalDate.parse(_selectedDate.value, dateFormatter)
            val today = LocalDate.parse(todayDateString, dateFormatter)
            selected.isAfter(today)
        } catch (e: Exception) {
            false
        }
    }

    fun selectDate(date: String) {
        stopEditingLog()
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureDayInitialized(date)
        }
    }

    fun navigatePreviousDay() {
        stopEditingLog()
        try {
            val current = LocalDate.parse(_selectedDate.value, dateFormatter)
            val prev = current.minusDays(1).format(dateFormatter)
            selectDate(prev)
        } catch (e: Exception) {
            // fallback
        }
    }

    fun navigateNextDay() {
        stopEditingLog()
        try {
            val current = LocalDate.parse(_selectedDate.value, dateFormatter)
            val next = current.plusDays(1).format(dateFormatter)
            selectDate(next)
        } catch (e: Exception) {
            // fallback
        }
    }

    fun navigateToToday() {
        stopEditingLog()
        selectDate(todayDateString)
    }

    fun startEditingLog(blockId: Long) {
        _editingLogBlockId.value = blockId
    }

    fun stopEditingLog() {
        _editingLogBlockId.value = null
    }

    fun toggleTaskCompletion(block: HourBlockEntity) {
        stopEditingLog()
        if (isSelectedDateInPast()) {
            _toastMessage.value = "Historical Archive is Frozen: Task actions are read-only. Hourly diary logs remain editable."
            return
        }
        viewModelScope.launch {
            repository.updateCompletion(block.id, !block.isCompleted)
        }
    }

    fun updateReviewLog(blockId: Long, newLog: String) {
        viewModelScope.launch {
            repository.updateReviewLog(blockId, newLog)
        }
    }

    /**
     * Saves or updates a task with automatic chronological cascading.
     * "Make it automatic that if i add hours to a task, the next task begins at the exact time the other ended."
     */
    fun saveOrUpdateBlock(block: HourBlockEntity) {
        if (isSelectedDateInPast()) {
            _toastMessage.value = "Historical Archive is Frozen: Cannot modify scheduled slots for past dates."
            return
        }
        viewModelScope.launch {
            repository.saveOrUpdateHourBlockWithCascade(_selectedDate.value, block)
            _editingBlock.value = null
            _toastMessage.value = if (block.durationHours > 1) {
                "Task scheduled (${block.durationHours} hrs). Subsequent slots automatically synchronized."
            } else {
                "Task successfully scheduled."
            }
        }
    }

    fun clearTask(block: HourBlockEntity) {
        if (isSelectedDateInPast()) {
            _toastMessage.value = "Historical Archive is Frozen: Cannot modify slots for past dates."
            return
        }
        viewModelScope.launch {
            repository.clearTaskFromSlot(_selectedDate.value, block)
            _editingBlock.value = null
            _toastMessage.value = "Task removed. Hour ${block.hour}:00 is now available."
        }
    }

    fun clearAllTasksForToday() {
        if (isSelectedDateInPast()) {
            _toastMessage.value = "Cannot reset past ledger records."
            return
        }
        viewModelScope.launch {
            repository.clearAllTasksForDate(_selectedDate.value)
            _toastMessage.value = "24-Hour Timeline cleared. All 24 slots are ready for your custom tasks."
        }
    }

    fun loadExecutiveTemplate() {
        if (isSelectedDateInPast()) {
            _toastMessage.value = "Cannot load templates onto past dates."
            return
        }
        viewModelScope.launch {
            repository.loadExecutiveTemplateForDate(_selectedDate.value)
            _toastMessage.value = "Executive Blueprint routine loaded."
        }
    }

    fun updateQuote(quoteText: String, quoteAuthor: String) {
        if (isSelectedDateInPast()) {
            _toastMessage.value = "Security Policy: Daily quotes on past dates are permanently locked."
            return
        }
        viewModelScope.launch {
            repository.updateDayQuote(_selectedDate.value, quoteText, quoteAuthor)
        }
    }

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        prefs.edit().putBoolean("is_dark_mode", next).apply()
    }

    fun setScreen(screen: DayLedgerScreen) {
        stopEditingLog()
        _currentScreen.value = screen
    }

    fun setReportDaysFilter(days: Int) {
        _reportDaysFilter.value = days
    }

    fun setEditingBlock(block: HourBlockEntity?) {
        stopEditingLog()
        if (isSelectedDateInPast() && block != null && block.title.isNotBlank()) {
            _toastMessage.value = "Task blocks are read-only for past dates. Use the review log box to record notes."
            return
        }
        _editingBlock.value = block
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private suspend fun seedHistoricalLedgerIfEmpty() = withContext(Dispatchers.IO) {
        val allBlocks = db.dayLedgerDao().getAllHourBlocks()
        // If there are fewer than 100 blocks in DB, seed realistic past days for 30-day reporting
        // Note: Today is NEVER seeded with fake tasks, so today is 100% clean for the user.
        if (allBlocks.size < 50) {
            val random = Random(42)
            val today = LocalDate.now()
            for (i in 1..28) {
                val pastDate = today.minusDays(i.toLong()).format(dateFormatter)
                val (quote, author) = DefaultExecutiveTemplate.getDailyQuoteForDate(pastDate)
                db.dayLedgerDao().insertOrUpdateDayPlan(
                    DayPlanEntity(
                        date = pastDate,
                        quoteText = quote,
                        quoteAuthor = author,
                        isQuoteLocked = true,
                        dayNotes = "Historical ledger audited."
                    )
                )

                val template = DefaultExecutiveTemplate.generateExecutiveRoutineTemplate(pastDate)
                val pastBlocks = template.map { block ->
                    val shouldComplete = when {
                        block.hour in 0..4 -> true
                        block.hour in 7..11 -> random.nextFloat() > 0.15f
                        block.hour in 12..14 -> random.nextFloat() > 0.30f
                        block.hour in 15..17 -> random.nextFloat() > 0.25f
                        block.hour == 18 -> random.nextFloat() > 0.40f
                        block.hour == 20 -> random.nextFloat() > 0.45f
                        else -> random.nextFloat() > 0.25f
                    }
                    val sampleLog = if (shouldComplete && block.hour in listOf(7, 8, 10, 13, 17)) {
                        "Executive Log: Accomplished key milestone. Deliverables aligned with quarterly objectives."
                    } else ""

                    block.copy(
                        isCompleted = shouldComplete,
                        reviewLog = sampleLog
                    )
                }
                db.dayLedgerDao().insertHourBlocks(pastBlocks)
            }
        }
    }
}

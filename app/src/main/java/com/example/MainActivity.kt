package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SidebarDrawer
import com.example.ui.components.TopCorporateStatusBar
import com.example.ui.screens.AuditArchiveScreen
import com.example.ui.screens.EditHourBlockDialog
import com.example.ui.screens.PerformanceReportsScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.DayLedgerTheme
import com.example.ui.viewmodel.DayLedgerScreen
import com.example.ui.viewmodel.DayLedgerViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: DayLedgerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            DayLedgerTheme(darkTheme = isDarkMode) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
                val currentTime by viewModel.currentTimeString.collectAsStateWithLifecycle()
                val currentDateFormatted by viewModel.currentDateFormatted.collectAsStateWithLifecycle()
                val currentHour by viewModel.currentHour.collectAsStateWithLifecycle()
                val hourBlocks by viewModel.hourBlocks.collectAsStateWithLifecycle()
                val dayPlan by viewModel.dayPlan.collectAsStateWithLifecycle()
                val validationResult by viewModel.validationResult.collectAsStateWithLifecycle()
                val performanceReport by viewModel.performanceReport.collectAsStateWithLifecycle()
                val reportDaysFilter by viewModel.reportDaysFilter.collectAsStateWithLifecycle()
                val editingBlock by viewModel.editingBlock.collectAsStateWithLifecycle()
                val editingLogBlockId by viewModel.editingLogBlockId.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

                var showDatePicker by remember { mutableStateOf(false) }

                // Handle back press
                BackHandler(enabled = drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                }

                BackHandler(enabled = !drawerState.isOpen && currentScreen != DayLedgerScreen.TIMELINE) {
                    viewModel.setScreen(DayLedgerScreen.TIMELINE)
                }

                // Show toast / feedback notifications
                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        SidebarDrawer(
                            currentScreen = currentScreen,
                            isDarkMode = isDarkMode,
                            onSelectScreen = { screen ->
                                viewModel.setScreen(screen)
                                scope.launch { drawerState.close() }
                            },
                            onToggleTheme = { viewModel.toggleDarkMode() },
                            onClearDay = {
                                viewModel.clearAllTasksForToday()
                                scope.launch { drawerState.close() }
                            },
                            onLoadTemplate = {
                                viewModel.loadExecutiveTemplate()
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopCorporateStatusBar(
                                currentTime = currentTime,
                                currentDate = currentDateFormatted,
                                currentHour = currentHour,
                                isDarkMode = isDarkMode,
                                onMenuClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                onToggleTheme = { viewModel.toggleDarkMode() }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                DayLedgerScreen.TIMELINE -> {
                                    TimelineScreen(
                                        selectedDate = selectedDate,
                                        todayDate = viewModel.todayDateString,
                                        isPastDate = viewModel.isSelectedDateInPast(),
                                        currentHour = currentHour,
                                        hourBlocks = hourBlocks,
                                        dayPlan = dayPlan,
                                        validationResult = validationResult,
                                        editingLogBlockId = editingLogBlockId,
                                        onStartEditingLog = { blockId -> viewModel.startEditingLog(blockId) },
                                        onStopEditingLog = { viewModel.stopEditingLog() },
                                        onPreviousDay = { viewModel.navigatePreviousDay() },
                                        onNextDay = { viewModel.navigateNextDay() },
                                        onGoToToday = { viewModel.navigateToToday() },
                                        onOpenCalendar = { showDatePicker = true },
                                        onToggleCompletion = { block -> viewModel.toggleTaskCompletion(block) },
                                        onEditBlock = { block -> viewModel.setEditingBlock(block) },
                                        onClearTask = { block -> viewModel.clearTask(block) },
                                        onUpdateReviewLog = { id, log -> viewModel.updateReviewLog(id, log) },
                                        onSaveQuote = { text, author -> viewModel.updateQuote(text, author) }
                                    )
                                }

                                DayLedgerScreen.PERFORMANCE_REPORTS -> {
                                    PerformanceReportsScreen(
                                        report = performanceReport,
                                        currentFilterDays = reportDaysFilter,
                                        onSelectFilterDays = { days -> viewModel.setReportDaysFilter(days) }
                                    )
                                }

                                DayLedgerScreen.AUDIT_ARCHIVE -> {
                                    AuditArchiveScreen(
                                        todayDate = viewModel.todayDateString,
                                        onSelectDate = { date ->
                                            viewModel.selectDate(date)
                                            viewModel.setScreen(DayLedgerScreen.TIMELINE)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Edit / Schedule Block Dialog
                editingBlock?.let { block ->
                    EditHourBlockDialog(
                        block = block,
                        onDismiss = { viewModel.setEditingBlock(null) },
                        onSave = { updated -> viewModel.saveOrUpdateBlock(updated) },
                        onClearTask = if (block.title.isNotBlank()) {
                            { viewModel.clearTask(block) }
                        } else null
                    )
                }

                // Date Picker Dialog
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val pickedDate = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        val formatted = pickedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US))
                                        viewModel.selectDate(formatted)
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("Select Date")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
        }
    }
}

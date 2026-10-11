package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DayPlanEntity
import com.example.data.local.HourBlockEntity
import com.example.domain.model.ValidationResult
import com.example.ui.components.DateNavigationBar
import com.example.ui.components.ExecutiveQuoteWidget
import com.example.ui.components.HourBlockCard
import com.example.ui.components.ValidationWarningBanner
import com.example.ui.theme.ActiveBlueHighlight
import com.example.ui.theme.CorporateGold
import com.example.ui.theme.EmeraldSuccess

@Composable
fun TimelineScreen(
    selectedDate: String,
    todayDate: String,
    isPastDate: Boolean,
    currentHour: Int,
    hourBlocks: List<HourBlockEntity>,
    dayPlan: DayPlanEntity?,
    validationResult: ValidationResult,
    editingLogBlockId: Long?,
    onStartEditingLog: (Long) -> Unit,
    onStopEditingLog: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToToday: () -> Unit,
    onOpenCalendar: () -> Unit,
    onToggleCompletion: (HourBlockEntity) -> Unit,
    onEditBlock: (HourBlockEntity) -> Unit,
    onClearTask: (HourBlockEntity) -> Unit,
    onUpdateReviewLog: (Long, String) -> Unit,
    onSaveQuote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isToday = selectedDate == todayDate

    // Auto-scroll to active hour on first load if viewing today
    LaunchedEffect(isToday, currentHour, hourBlocks.size) {
        if (isToday && hourBlocks.isNotEmpty()) {
            val activeIndex = hourBlocks.indexOfFirst {
                (currentHour * 60) >= it.startMinuteOfDay && (currentHour * 60) < it.endMinuteOfDay
            }
            if (activeIndex >= 0) {
                listState.animateScrollToItem((activeIndex + 3).coerceAtMost(hourBlocks.size + 2))
            }
        }
    }

    val scheduledTasks = hourBlocks.filter { it.title.isNotBlank() }
    val completedCount = scheduledTasks.count { it.isCompleted }
    val totalCount = scheduledTasks.size
    val completionPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()) * 100f else 0f

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("timeline_lazy_column")
    ) {
        // Date Navigation Strip
        item {
            DateNavigationBar(
                selectedDate = selectedDate,
                todayDate = todayDate,
                isPastDate = isPastDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onGoToToday = onGoToToday,
                onOpenCalendar = onOpenCalendar
            )
        }

        // Past Date Frozen Audit Notice
        if (isPastDate) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, CorporateGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Historical Audit Mode",
                        tint = CorporateGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HISTORICAL AUDIT ARCHIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CorporateGold
                        )
                        Text(
                            text = "Scheduled tasks are frozen and read-only. Hourly review diary text boxes remain fully editable for your retrospective audit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Daily Locked Executive Quote Widget
        item {
            ExecutiveQuoteWidget(
                quoteText = dayPlan?.quoteText ?: "",
                quoteAuthor = dayPlan?.quoteAuthor ?: "",
                isPastDate = isPastDate,
                onSaveQuote = onSaveQuote
            )
        }

        // Daily Completion Progress Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "24-HOUR EXECUTION RATIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (totalCount > 0)
                                "$completedCount of $totalCount Scheduled (${completionPercent.toInt()}%)"
                            else
                                "No tasks scheduled yet",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (completionPercent >= 70f && totalCount > 0) EmeraldSuccess else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) (completionPercent / 100f).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (completionPercent >= 70f) EmeraldSuccess else ActiveBlueHighlight,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Timeline Integrity Validation Banner
        item {
            ValidationWarningBanner(validationResult = validationResult)
        }

        // Timeline Section Header & Quick Add Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "24-HOUR CONTINUOUS TIMELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "00:00 → 24:00 • Custom scheduled tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isPastDate) {
                    val firstEmptySlot = hourBlocks.firstOrNull { it.title.isBlank() }
                    Button(
                        onClick = {
                            onStopEditingLog()
                            firstEmptySlot?.let { onEditBlock(it) }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 142.dp)
                            .height(42.dp)
                            .testTag("quick_schedule_task_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Task",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Continuous 24-Hour Blocks Feed
        items(
            items = hourBlocks,
            key = { "${it.date}_${it.hour}_${it.minute}_${it.id}" }
        ) { block ->
            val isActiveNow = isToday && ((currentHour * 60) >= block.startMinuteOfDay && (currentHour * 60) < block.endMinuteOfDay)
            val hasConflict = validationResult.conflictingHours.contains(block.hour)

            HourBlockCard(
                block = block,
                isActiveNow = isActiveNow,
                isPastDate = isPastDate,
                hasConflict = hasConflict,
                isEditingLog = (editingLogBlockId == block.id),
                onStartEditLog = { onStartEditingLog(block.id) },
                onStopEditLog = onStopEditingLog,
                onToggleCompletion = { onToggleCompletion(block) },
                onEditBlock = { onEditBlock(block) },
                onClearTask = { onClearTask(block) },
                onUpdateReviewLog = { log -> onUpdateReviewLog(block.id, log) }
            )
        }
    }
}

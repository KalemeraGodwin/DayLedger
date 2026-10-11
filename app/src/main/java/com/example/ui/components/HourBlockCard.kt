package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HourBlockEntity
import com.example.ui.theme.ActiveBlueGlow
import com.example.ui.theme.ActiveBlueHighlight
import com.example.ui.theme.CorporateGold
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.WarningAmber

@Composable
fun HourBlockCard(
    block: HourBlockEntity,
    isActiveNow: Boolean,
    isPastDate: Boolean,
    hasConflict: Boolean,
    isEditingLog: Boolean,
    onStartEditLog: () -> Unit,
    onStopEditLog: () -> Unit,
    onToggleCompletion: () -> Unit,
    onEditBlock: () -> Unit,
    onClearTask: () -> Unit,
    onUpdateReviewLog: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var reviewTextInput by remember(block.reviewLog) { mutableStateOf(block.reviewLog) }

    // If block reviewLog changes from database, sync reviewTextInput
    androidx.compose.runtime.LaunchedEffect(block.reviewLog) {
        if (!isEditingLog) {
            reviewTextInput = block.reviewLog
        }
    }

    val isTaskAssigned = block.title.isNotBlank()
    val startTimeStr = block.formatStartTime()
    val endTimeStr = block.formatEndTime()
    val durationLabel = block.formatDuration()

    val borderColor by animateColorAsState(
        targetValue = when {
            hasConflict -> CrimsonError
            isActiveNow -> if (MaterialTheme.colorScheme.background == Color(0xFF030712)) ActiveBlueGlow else ActiveBlueHighlight
            isTaskAssigned && block.isCompleted -> EmeraldSuccess.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline
        },
        label = "borderColor"
    )

    val borderWidth = if (isActiveNow || hasConflict) 2.dp else 1.dp

    Surface(
        color = if (isActiveNow) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                // If user clicks on card, dismiss any open log edit in other cards
                if (!isEditingLog) {
                    onStopEditLog()
                }
                if (!isTaskAssigned && !isPastDate) {
                    onEditBlock()
                }
            }
            .testTag("hour_block_card_${block.hour}_${block.minute}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Slot Metadata Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time Range & Active Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$startTimeStr – $endTimeStr",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("hour_time_label_${block.hour}_${block.minute}")
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = durationLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (isActiveNow) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ActiveBlueHighlight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("active_hour_pill")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE NOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    if (hasConflict) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CrimsonError)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "COLLISION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Controls and Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTaskAssigned) {
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = block.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Priority Pill
                        val priorityColor = when (block.priority.uppercase()) {
                            "HIGH" -> CrimsonError
                            "MEDIUM" -> WarningAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = block.priority,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor
                        )

                        if (!isPastDate) {
                            Spacer(modifier = Modifier.width(4.dp))
                            // Edit Block button
                            IconButton(
                                onClick = {
                                    onStopEditLog()
                                    onEditBlock()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("edit_block_button_${block.hour}_${block.minute}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Block",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Clear Block button
                            IconButton(
                                onClick = {
                                    onStopEditLog()
                                    onClearTask()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("clear_block_button_${block.hour}_${block.minute}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear Task",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Frozen in Past",
                                tint = CorporateGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // Empty / Unscheduled Slot Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "AVAILABLE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Body: Task action or "+ Add Task" affordance
            if (isTaskAssigned) {
                // Scheduled Action Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isPastDate) {
                            onStopEditLog()
                            onToggleCompletion()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = block.isCompleted,
                        onCheckedChange = {
                            if (!isPastDate) {
                                onStopEditLog()
                                onToggleCompletion()
                            }
                        },
                        enabled = !isPastDate,
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldSuccess,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("hour_checkbox_${block.hour}_${block.minute}")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (block.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (block.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (block.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (block.isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            } else {
                // Available / Unscheduled Slot: User adds task manually
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable(enabled = !isPastDate) {
                            onStopEditLog()
                            onEditBlock()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Available Slot",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "$startTimeStr – $endTimeStr • Open for scheduling",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    if (!isPastDate) {
                        Button(
                            onClick = {
                                onStopEditLog()
                                onEditBlock()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 142.dp)
                                .height(42.dp)
                                .testTag("add_task_slot_button_${block.hour}_${block.minute}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                                modifier = Modifier.size(18.dp)
                            )
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

            // Granular Review Logs / Diary Section (Dedicated Save / Edit Lifecycle)
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                // Header of log section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Review Log",
                            tint = if (block.reviewLog.isNotEmpty()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPastDate) "HISTORICAL DIARY AUDIT" else "HOURLY REVIEW & EXECUTIVE DEBRIEF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isEditingLog) {
                        if (block.reviewLog.isNotBlank()) {
                            // Edit Log button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        reviewTextInput = block.reviewLog
                                        onStartEditLog()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Log",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "EDIT LOG",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            // Add Log button
                            Text(
                                text = "+ ADD LOG",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable {
                                        reviewTextInput = ""
                                        onStartEditLog()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Body: View Mode vs Edit Mode
                if (isEditingLog) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reviewTextInput,
                        onValueChange = { reviewTextInput = it },
                        placeholder = {
                            Text(
                                text = if (isPastDate)
                                    "Audit accomplishments, challenges, or notes for this slot..."
                                else
                                    "Log accomplishments, metrics, or retrospective notes...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_log_input_${block.hour}_${block.minute}"),
                        textStyle = MaterialTheme.typography.bodySmall,
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Explicit Save Log and Cancel Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${reviewTextInput.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    reviewTextInput = block.reviewLog
                                    onStopEditLog()
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel", style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    onUpdateReviewLog(reviewTextInput.trim())
                                    onStopEditLog()
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("save_log_button_${block.hour}_${block.minute}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save Log", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Log", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                } else if (block.reviewLog.isNotBlank()) {
                    // Sealed Saved Display Mode
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = block.reviewLog,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

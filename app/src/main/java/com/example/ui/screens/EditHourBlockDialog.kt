package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HourBlockEntity
import com.example.domain.model.DefaultExecutiveTemplate
import com.example.ui.theme.ActiveBlueHighlight
import com.example.ui.theme.CrimsonError

@Composable
fun EditHourBlockDialog(
    block: HourBlockEntity,
    onDismiss: () -> Unit,
    onSave: (HourBlockEntity) -> Unit,
    onClearTask: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(block.title) }
    var category by remember {
        mutableStateOf(
            if (block.category == "General" || block.category == "Unassigned") "Deep Work" else block.category
        )
    }
    var priority by remember { mutableStateOf(block.priority) }
    var startHour by remember { mutableIntStateOf(block.hour.coerceIn(0, 23)) }
    var startMinute by remember { mutableIntStateOf(if (block.minute >= 30) 30 else 0) }
    var durationMinutes by remember {
        mutableIntStateOf(
            if (block.durationMinutes >= 30) block.durationMinutes else 60
        )
    }

    val isNewTask = block.title.isBlank()

    val startTotalMin = startHour * 60 + startMinute
    val endTotalMin = startTotalMin + durationMinutes
    val endHourClamped = (endTotalMin / 60).coerceAtMost(24)
    val endMinuteClamped = endTotalMin % 60

    val startTimeStr = String.format("%02d:%02d", startHour, startMinute)
    val endTimeStr = String.format("%02d:%02d", endHourClamped, endMinuteClamped)

    val durationLabel = when {
        durationMinutes >= 60 && durationMinutes % 60 == 0 -> "${durationMinutes / 60} hr${if (durationMinutes / 60 > 1) "s" else ""}"
        durationMinutes >= 60 -> "${durationMinutes / 60}h ${durationMinutes % 60}m"
        else -> "${durationMinutes} min"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isNewTask) "SCHEDULE NEW TASK" else "EDIT TASK BLOCK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$startTimeStr – $endTimeStr ($durationLabel)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task / Activity Name") },
                    placeholder = { Text("e.g., Executive Strategy, Deep Focus") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_block_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Start Time & 30-Minute Minute Increment Selector
                Text(
                    text = "START TIME (30-MIN INCREMENTS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour Stepper
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (startHour > 0) startHour-- },
                            enabled = startHour > 0,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Prev Hour", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = String.format("%02d", startHour),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = { if (startHour < 23) startHour++ },
                            enabled = startHour < 23,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Next Hour", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Minute 00 / 30 Toggle Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to ":00", 30 to ":30").forEach { (minVal, label) ->
                            val isSelected = startMinute == minVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .clickable { startMinute = minVal }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Stepper (30-Minute Increments)
                Text(
                    text = "DURATION (30-MIN INCREMENTS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$durationLabel ($startTimeStr – $endTimeStr)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (durationMinutes > 30) durationMinutes -= 30
                            },
                            enabled = durationMinutes > 30,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease 30m", modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                if (startTotalMin + durationMinutes + 30 <= 1440) durationMinutes += 30
                            },
                            enabled = startTotalMin + durationMinutes + 30 <= 1440,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase 30m", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Quick Duration Chips (30m, 1h, 1h 30m, 2h, 3h)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        30 to "30m",
                        60 to "1h",
                        90 to "1.5h",
                        120 to "2h",
                        180 to "3h"
                    ).forEach { (mins, chipLabel) ->
                        val isSelected = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable { durationMinutes = mins }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chipLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Automatic Cascading Notice
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ActiveBlueHighlight.copy(alpha = 0.08f))
                        .border(1.dp, ActiveBlueHighlight.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Auto Shift",
                        tint = ActiveBlueHighlight,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Automatic Cascade: Subsequent tasks will begin at $endTimeStr.",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ActiveBlueHighlight
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selection
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(DefaultExecutiveTemplate.CATEGORIES) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isNewTask && onClearTask != null) {
                    OutlinedButton(
                        onClick = onClearTask,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }

                Button(
                    onClick = {
                        val finalTitle = title.trim()
                        onSave(
                            block.copy(
                                hour = startHour,
                                minute = startMinute,
                                durationMinutes = durationMinutes,
                                durationHours = (durationMinutes + 59) / 60,
                                title = finalTitle.ifEmpty { "Task at $startTimeStr" },
                                category = category,
                                priority = priority,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 145.dp)
                        .height(42.dp)
                        .testTag("save_block_dialog_button")
                ) {
                    Text(if (isNewTask) "Schedule Task" else "Save Changes")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

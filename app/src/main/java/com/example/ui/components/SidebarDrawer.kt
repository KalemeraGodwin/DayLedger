package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActiveBlueHighlight
import com.example.ui.theme.CorporateGold
import com.example.ui.theme.CorporateNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.viewmodel.DayLedgerScreen

@Composable
fun SidebarDrawer(
    currentScreen: DayLedgerScreen,
    isDarkMode: Boolean,
    onSelectScreen: (DayLedgerScreen) -> Unit,
    onToggleTheme: () -> Unit,
    onClearDay: () -> Unit,
    onLoadTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .border(width = 1.dp, color = borderCol)
            .padding(20.dp)
            .testTag("sidebar_navigation_drawer")
    ) {
        // Corporate Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CorporateNavy)
                    .border(1.dp, CorporateGold, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "DayLedger Emblem",
                    tint = CorporateGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "DAYLEDGER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Executive 24-Hour Codex",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = borderCol, thickness = 1.dp)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "NAVIGATION CONSOLE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Items
        DrawerNavItem(
            title = "24-Hour Day Ledger",
            subtitle = "Continuous 24h timeline & diary",
            icon = Icons.Default.Schedule,
            isSelected = currentScreen == DayLedgerScreen.TIMELINE,
            testTag = "drawer_nav_timeline",
            onClick = { onSelectScreen(DayLedgerScreen.TIMELINE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerNavItem(
            title = "Performance Reports",
            subtitle = "30-day analytics & completion trends",
            icon = Icons.Default.Assessment,
            isSelected = currentScreen == DayLedgerScreen.PERFORMANCE_REPORTS,
            testTag = "drawer_nav_reports",
            onClick = { onSelectScreen(DayLedgerScreen.PERFORMANCE_REPORTS) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerNavItem(
            title = "Audit Archive",
            subtitle = "Historical dates & locked review audit",
            icon = Icons.Default.CalendarMonth,
            isSelected = currentScreen == DayLedgerScreen.AUDIT_ARCHIVE,
            testTag = "drawer_nav_archive",
            onClick = { onSelectScreen(DayLedgerScreen.AUDIT_ARCHIVE) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = borderCol, thickness = 1.dp)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "EXECUTIVE CONTROLS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dark/Light Theme Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onToggleTheme() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                    contentDescription = "Theme",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isDarkMode) "Obsidian Navy" else "Canvas White",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Dark Theme Active" else "Light Theme Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggleTheme() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CorporateGold,
                    checkedTrackColor = CorporateNavy,
                    uncheckedThumbColor = CorporateNavy,
                    uncheckedTrackColor = borderCol
                ),
                modifier = Modifier.testTag("drawer_theme_switch")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clear Day Schedule (Start Clean)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                .clickable { onClearDay() }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("drawer_clear_day_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Clear Schedule",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Start clean with all 24 slots open",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Load Executive Routine Template (Optional)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                .clickable { onLoadTemplate() }
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("drawer_load_template_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Load Template",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Load Routine Template",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pre-fill standard executive blueprint",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer Metadata
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(EmeraldSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OFFLINE DATABASE ENCRYPTED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldSuccess
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "100% On-Device Room Storage • Zero external cloud dependency.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

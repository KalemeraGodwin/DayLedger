package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyPerformancePoint
import com.example.ui.theme.ActiveBlueGlow
import com.example.ui.theme.ActiveBlueHighlight
import com.example.ui.theme.CorporateNavy
import com.example.ui.theme.CorporateNavyLight
import com.example.ui.theme.EmeraldSuccess
import kotlin.math.roundToInt

@Composable
fun CorporateTrendChart(
    points: List<DailyPerformancePoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val linePrimaryColor = if (MaterialTheme.colorScheme.background == Color(0xFF030712)) ActiveBlueGlow else ActiveBlueHighlight
    val gridColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .testTag("corporate_trend_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart Header & Interactive Tooltip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30-DAY PERFORMANCE TRAJECTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Daily Completion Percentage Trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Interactive Inspection Pill
                val inspected = selectedIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
                if (inspected != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("chart_tooltip_pill")
                    ) {
                        Text(
                            text = "${inspected.shortLabel}: ${inspected.completionRate.roundToInt()}% (${inspected.completedCount}/${inspected.totalCount})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart Area
            if (points.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .pointerInput(points) {
                                detectTapGestures { tapOffset ->
                                    val pointSpacing = size.width / (points.size.coerceAtLeast(2) - 1).toFloat()
                                    val index = ((tapOffset.x + (pointSpacing / 2f)) / pointSpacing)
                                        .toInt()
                                        .coerceIn(0, points.size - 1)
                                    selectedIndex = index
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val paddingBottom = 24.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val chartHeight = height - paddingBottom - paddingTop

                        // Draw Grid lines at 100%, 75%, 50%, 25%, 0%
                        val gridLevels = listOf(1.0f, 0.75f, 0.5f, 0.25f, 0.0f)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                        gridLevels.forEach { level ->
                            val y = paddingTop + (1f - level) * chartHeight
                            drawLine(
                                color = gridColor.copy(alpha = 0.6f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f,
                                pathEffect = dashEffect
                            )
                        }

                        // Calculate points coordinates
                        val pointCoords = mutableListOf<Offset>()
                        val stepX = width / (points.size - 1).coerceAtLeast(1)

                        points.forEachIndexed { i, point ->
                            val x = i * stepX
                            val normalizedY = (point.completionRate / 100f).coerceIn(0f, 1f)
                            val y = paddingTop + (1f - normalizedY) * chartHeight
                            pointCoords.add(Offset(x, y))
                        }

                        // Build Gradient Area Path
                        val fillPath = Path().apply {
                            moveTo(pointCoords.first().x, paddingTop + chartHeight)
                            pointCoords.forEach { pt ->
                                lineTo(pt.x, pt.y)
                            }
                            lineTo(pointCoords.last().x, paddingTop + chartHeight)
                            close()
                        }

                        val fillBrush = Brush.verticalGradient(
                            colors = listOf(
                                linePrimaryColor.copy(alpha = 0.35f),
                                linePrimaryColor.copy(alpha = 0.03f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + chartHeight
                        )
                        drawPath(path = fillPath, brush = fillBrush)

                        // Build Stroke Path with cubic curves for smoothness
                        val strokePath = Path().apply {
                            moveTo(pointCoords.first().x, pointCoords.first().y)
                            for (i in 0 until pointCoords.size - 1) {
                                val p0 = pointCoords[i]
                                val p1 = pointCoords[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = strokePath,
                            color = linePrimaryColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Highlight Data Points
                        pointCoords.forEachIndexed { i, pt ->
                            val isSelected = selectedIndex == i || (selectedIndex == null && i == pointCoords.lastIndex)
                            if (isSelected) {
                                // Outer pulse ring
                                drawCircle(
                                    color = linePrimaryColor.copy(alpha = 0.25f),
                                    radius = 10.dp.toPx(),
                                    center = pt
                                )
                                // Solid center ring
                                drawCircle(
                                    color = linePrimaryColor,
                                    radius = 5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = pt
                                )
                            } else if (i % 5 == 0 || i == pointCoords.lastIndex) {
                                drawCircle(
                                    color = linePrimaryColor,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }

                // X-Axis Date markers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val step = (points.size / 5).coerceAtLeast(1)
                    for (i in points.indices step step) {
                        Text(
                            text = points[i].shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor
                        )
                    }
                    if ((points.size - 1) % step != 0) {
                        Text(
                            text = points.last().shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Collecting 30-day timeline data points...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Footer Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(linePrimaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calculated Execution Ratio",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }

                Text(
                    text = "Tap node to inspect date",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

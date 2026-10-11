package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ActiveBlueGlow,
    onPrimary = ObsidianNavy,
    primaryContainer = DeepBlueGrayElevated,
    onPrimaryContainer = PureWhite,
    secondary = CorporateGold,
    onSecondary = ObsidianNavy,
    tertiary = ActiveBlueHighlight,
    onTertiary = PureWhite,
    background = ObsidianNavy,
    onBackground = PureWhite,
    surface = DeepBlueGray,
    onSurface = PureWhite,
    surfaceVariant = DeepBlueGrayElevated,
    onSurfaceVariant = SteelMuted,
    outline = SlateNavyBorder,
    outlineVariant = SlateNavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CorporateNavy,
    onPrimary = CanvasWhite,
    primaryContainer = PremiumIceBlue,
    onPrimaryContainer = CorporateNavy,
    secondary = CorporateNavyLight,
    onSecondary = CanvasWhite,
    tertiary = ActiveBlueHighlight,
    onTertiary = CanvasWhite,
    background = CanvasWhite,
    onBackground = MidnightText,
    surface = PremiumIceBlue,
    onSurface = MidnightText,
    surfaceVariant = CanvasWhite,
    onSurfaceVariant = SlateMuted,
    outline = CleanSlate,
    outlineVariant = CleanSlate
)

@Composable
fun DayLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

package com.nextdoor.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Olive600,
    onPrimary = Color.White,
    primaryContainer = Olive50,
    onPrimaryContainer = Olive800,
    secondary = Olive700,
    onSecondary = Color.White,
    secondaryContainer = Olive100,
    onSecondaryContainer = Olive800,
    tertiary = TextPrimary,
    background = NaturalBg,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = NaturalCard,
    onSurfaceVariant = TextMuted,
    outline = NaturalBorder,
    outlineVariant = NaturalDivider,
    error = Rose700,
    onError = Color.White,
    errorContainer = Rose50,
    onErrorContainer = Rose800
)

/**
 * App-specific, non-Material color tokens exposed via composition local.
 */
data class NextDoorColors(
    val olive50: Color = Olive50,
    val olive100: Color = Olive100,
    val olive200: Color = Olive200,
    val olive600: Color = Olive600,
    val olive700: Color = Olive700,
    val olive800: Color = Olive800,
    val naturalBg: Color = NaturalBg,
    val naturalCard: Color = NaturalCard,
    val naturalBorder: Color = NaturalBorder,
    val naturalDivider: Color = NaturalDivider,
    val textPrimary: Color = TextPrimary,
    val textMuted: Color = TextMuted,
    // Status pairs
    val amber: Pair<Color, Color> = Amber50 to Amber700,
    val blue: Pair<Color, Color> = Blue50 to Blue700,
    val purple: Pair<Color, Color> = Purple50 to Purple700,
    val emerald: Pair<Color, Color> = Emerald50 to Emerald700,
    val rose: Pair<Color, Color> = Rose50 to Rose700,
)
val LocalNextDoorColors = staticCompositionLocalOf { NextDoorColors() }

@Composable
fun NextDoorTheme(content: @Composable () -> Unit) {
    // Light-only per plan (the front-end has no dark mode). Keep a minimal dark
    // variant switch if the OS requests it, but default to light styling.
    val view = LocalView.current
    if (!view.isInEditMode) {
        WindowCompat.getInsetsController((view.context as Activity).window, view)
            .isAppearanceLightStatusBars = true
    }

    CompositionLocalProvider(LocalNextDoorColors provides NextDoorColors()) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = NextDoorTypography,
            shapes = NextDoorShapes,
            content = content
        )
    }
}

object NextDoorTheme {
    val colors: NextDoorColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNextDoorColors.current
}

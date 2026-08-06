package com.nextdoor.app.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Font families used across the app.
 *
 * The front-end design uses "Playfair Display" (serif) for headings/prices and
 * "Plus Jakarta Sans" (sans) for UI/body copy. We use the system serif / sans
 * families as a robust fallback; swap in downloadable Google Fonts later by
 * replacing these with `GoogleFont("Playfair Display", ...)`/`GoogleFont(...)`
 * via the Compose downloadable-fonts provider (both share the same API surface
 * for Typography, so no call-site changes are needed).
 */
val SerifFamily = FontFamily.Serif
val SansFamily = FontFamily.SansSerif

// Convenience fonts matching the design intent.
val FontSerifTitle = FontFamily.Serif
val FontSerifItalic = FontFamily.Serif
val FontBody = FontFamily.SansSerif

// Weight references so text styles can target the typographic intent even when
// falling back to system families (which map these to available weights).
object FontWeights {
    val Bold = FontWeight.Bold
    val ExtraBold = FontWeight.ExtraBold
}

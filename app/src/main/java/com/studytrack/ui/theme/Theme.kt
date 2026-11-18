package com.studytrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudyTrackDarkColorScheme = darkColorScheme(
    // Primary – brand red
    primary = StudyRed,
    onPrimary = OnPrimary,
    primaryContainer = StudyRedDark,
    onPrimaryContainer = StudyRedLight,

    // Secondary / accent
    secondary = Accent,
    onSecondary = OnPrimary,
    secondaryContainer = AccentContainer,
    onSecondaryContainer = OnSurface,

    // Tertiary
    tertiary = AccentContainer,
    onTertiary = OnSurface,

    // Background
    background = BackgroundDeepNavy,
    onBackground = OnBackground,

    // Surface
    surface = SurfacePurpleNight,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    // Outline
    outline = Outline,

    // Error
    error = Error,
    onError = OnPrimary
)

/**
 * StudyTrack always uses the dark theme.
 */
@Composable
fun StudyTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyTrackDarkColorScheme,
        typography = StudyTrackTypography,
        content = content
    )
}

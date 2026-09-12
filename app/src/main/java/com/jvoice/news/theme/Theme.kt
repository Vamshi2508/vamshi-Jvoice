package com.jvoice.news.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = JvRed,
    onPrimary = Color.White,
    primaryContainer = JvRedLight,
    onPrimaryContainer = JvRedDark,
    secondary = JvSaffron,
    onSecondary = Color.White,
    secondaryContainer = JvSaffronLight,
    onSecondaryContainer = Color(0xFF4A2800),
    tertiary = JvGreen,
    onTertiary = Color.White,
    tertiaryContainer = JvGreenLight,
    onTertiaryContainer = Color(0xFF00311A),
    background = JvSurfaceLight,
    onBackground = JvInk,
    surface = JvSurfaceLight,
    onSurface = JvInk,
    surfaceVariant = JvSurfaceVariantLight,
    onSurfaceVariant = JvInkSoft,
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C6D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = JvRedLight,
    secondary = Color(0xFFFFB86B),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3B00),
    onSecondaryContainer = JvSaffronLight,
    tertiary = Color(0xFF7ED9A6),
    onTertiary = Color(0xFF00391D),
    tertiaryContainer = Color(0xFF005230),
    onTertiaryContainer = JvGreenLight,
    background = JvSurfaceDark,
    onBackground = Color(0xFFE5E1E6),
    surface = JvSurfaceDark,
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = JvSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC6C6D0),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF44464F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun JVoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = JvTypography,
        content = content
    )
}

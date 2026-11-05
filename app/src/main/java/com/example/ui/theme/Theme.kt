package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Material 3 Expressive Dark Color Scheme (Pure OLED Black #000000)
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E1E28),
    onPrimaryContainer = Color(0xFFF1F5F9),
    secondary = ExpressiveCyanGlow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF13131A),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = ExpressiveElectricIndigo,
    onTertiary = Color.White,
    background = Color(0xFF000000),      // Pure OLED True Black
    onBackground = Color.White,
    surface = Color(0xFF000000),         // Pure OLED True Black
    onSurface = Color.White,
    surfaceVariant = Color(0xFF13131A),  // Deep expressive graphite surface
    onSurfaceVariant = Color(0xFF94A3B8),// Slate 400
    surfaceContainer = Color(0xFF181822),
    surfaceContainerHigh = Color(0xFF222230),
    surfaceContainerHighest = Color(0xFF2C2C3D),
    outline = Color(0xFF262634),
    outlineVariant = Color(0x28FFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F172A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = ExpressiveElectricIndigo,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // App is dark-themed on pure OLED black, so status bar and navigation bar icons are light
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

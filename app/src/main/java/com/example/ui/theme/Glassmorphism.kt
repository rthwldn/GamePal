package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =========================================================================
// Material 3 Expressive - OLED Black Depth & High-Contrast Brushes
// =========================================================================

// High-contrast expressive card background that floats seamlessly over pure OLED #000000
private val DarkCardDefaultBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF15151E), // Subtle specular top edge
        Color(0xFF101017), // Rich body
        Color(0xFF0A0A0E)  // Deep bottom base
    )
)

private val DarkCardDefaultBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x38FFFFFF), // 22% white crisp top hairline rim
        Color(0x12FFFFFF)  // 7% subtle bottom rim
    )
)

private val LightCardDefaultBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xF2FFFFFF),
        Color(0xD9F4F4F5)
    )
)

private val LightCardDefaultBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xD9FFFFFF),
        Color(0x40FFFFFF)
    )
)

// Expressive glowing accent border (Electric Indigo -> Cyan Glow -> Specular White)
private val AccentBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF818CF8),
        Color(0xFF38BDF8),
        Color(0x40FFFFFF)
    )
)

private val DarkPillDefaultBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1C1C26),
        Color(0xFF14141C)
    )
)

private val DarkPillDefaultBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x40FFFFFF),
        Color(0x18FFFFFF)
    )
)

@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    isDark: Boolean = true,
    elevation: Dp = if (isDark) 0.dp else 2.dp,
    accentBorder: Boolean = false,
    customTint: Color? = null
): Modifier {
    val backgroundBrush = remember(isDark, customTint) {
        if (customTint != null) {
            if (isDark) {
                Brush.verticalGradient(
                    colors = listOf(
                        customTint.copy(alpha = 0.32f),
                        customTint.copy(alpha = 0.16f),
                        Color(0xFF0A0A0E)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        customTint.copy(alpha = 0.95f),
                        Color(0xFFF4F4F5).copy(alpha = 0.85f)
                    )
                )
            }
        } else {
            if (isDark) DarkCardDefaultBackgroundBrush else LightCardDefaultBackgroundBrush
        }
    }

    val borderBrush = remember(isDark, accentBorder) {
        if (accentBorder) {
            AccentBorderBrush
        } else {
            if (isDark) DarkCardDefaultBorderBrush else LightCardDefaultBorderBrush
        }
    }

    return this
        .then(
            if (!isDark && elevation > 0.dp) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = Color(0x1A09090B),
                    ambientColor = Color(0x0D09090B)
                )
            } else Modifier
        )
        .clip(shape)
        .background(backgroundBrush)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
}

@Composable
fun Modifier.glassPill(
    shape: Shape = RoundedCornerShape(percent = 50),
    isDark: Boolean = true,
    accentBorder: Boolean = false,
    customTint: Color? = null
): Modifier {
    val backgroundBrush = remember(isDark, customTint) {
        if (customTint != null) {
            val baseColor = customTint
            Brush.verticalGradient(
                colors = listOf(
                    baseColor.copy(alpha = if (isDark) 0.38f else 0.95f),
                    baseColor.copy(alpha = if (isDark) 0.20f else 0.85f)
                )
            )
        } else {
            if (isDark) DarkPillDefaultBackgroundBrush else LightCardDefaultBackgroundBrush
        }
    }

    val borderBrush = remember(isDark, accentBorder) {
        if (accentBorder) {
            AccentBorderBrush
        } else {
            if (isDark) DarkPillDefaultBorderBrush else LightCardDefaultBorderBrush
        }
    }

    return this
        .clip(shape)
        .background(backgroundBrush)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
}

@Composable
fun Modifier.auroraBackground(isDark: Boolean = true): Modifier {
    // 100% True OLED Pitch Black (#000000) for maximal battery savings and infinite contrast
    return this.background(if (isDark) Color(0xFF000000) else Color(0xFFF8FAFC))
}

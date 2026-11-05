package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Pre-allocated static brushes to eliminate memory allocations on every frame during scrolling
private val DarkCardDefaultBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1A1A1E), // Subtle top specular shine
        Color(0xFF131316), // Mid body
        Color(0xFF0D0D10)  // Bottom depth
    )
)

private val DarkCardDefaultBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x33FFFFFF), // Crisp top hairline rim
        Color(0x0FFFFFFF)  // Soft bottom rim
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

private val AccentBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xB3FFFFFF),
        Color(0x66A1A1AA),
        Color(0x26FFFFFF)
    )
)

private val DarkPillDefaultBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x591E1E23),
        Color(0x2E1E1E23)
    )
)

private val DarkPillDefaultBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x40FFFFFF),
        Color(0x14FFFFFF)
    )
)

@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(12.dp),
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
                        customTint.copy(alpha = 0.25f),
                        customTint.copy(alpha = 0.15f),
                        Color(0xFF0D0D10)
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
    shape: Shape = RoundedCornerShape(8.dp),
    isDark: Boolean = true,
    accentBorder: Boolean = false,
    customTint: Color? = null
): Modifier {
    val backgroundBrush = remember(isDark, customTint) {
        if (customTint != null) {
            val baseColor = customTint
            Brush.verticalGradient(
                colors = listOf(
                    baseColor.copy(alpha = if (isDark) 0.35f else 0.95f),
                    baseColor.copy(alpha = if (isDark) 0.18f else 0.85f)
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
    // Ultra-fast solid background fill with zero GPU shader allocation
    return this.background(if (isDark) Color(0xFF09090B) else Color(0xFFF4F4F5))
}

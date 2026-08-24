package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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

@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(20.dp),
    isDark: Boolean = true,
    elevation: Dp = if (isDark) 0.dp else 4.dp,
    accentBorder: Boolean = false,
    customTint: Color? = null
): Modifier {
    val backgroundBrush = if (isDark) {
        val baseColor = customTint ?: Color(0xFF161B26)
        Brush.verticalGradient(
            colors = listOf(
                (customTint?.copy(alpha = 0.35f) ?: Color(0x1FFFFFFF)), // Top specular shine
                (customTint?.copy(alpha = 0.20f) ?: Color(0x0CFFFFFF)), // Mid body
                Color(0x140F1117) // Bottom shadow blend
            )
        )
    } else {
        val baseColor = customTint ?: Color.White
        Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.90f),
                Color(0xFFF8FAFC).copy(alpha = 0.75f)
            )
        )
    }

    val borderBrush = if (accentBorder) {
        Brush.linearGradient(
            colors = listOf(
                GamePalPrimary.copy(alpha = 0.7f),
                GamePalSecondary.copy(alpha = 0.5f),
                Color.White.copy(alpha = if (isDark) 0.2f else 0.5f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.28f else 0.85f), // Crisp top highlight rim
                Color.White.copy(alpha = if (isDark) 0.08f else 0.30f)  // Soft bottom edge
            )
        )
    }

    return this
        .then(
            if (!isDark && elevation > 0.dp) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = Color(0x1A0F172A),
                    ambientColor = Color(0x0D0F172A)
                )
            } else Modifier
        )
        .clip(shape)
        .background(backgroundBrush)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
}

@Composable
fun Modifier.glassPill(
    shape: Shape = RoundedCornerShape(50),
    isDark: Boolean = true,
    accentBorder: Boolean = false,
    customTint: Color? = null
): Modifier {
    val backgroundBrush = if (isDark) {
        val baseColor = customTint ?: Color(0x1AFFFFFF)
        Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.25f),
                baseColor.copy(alpha = 0.10f)
            )
        )
    } else {
        val baseColor = customTint ?: Color.White
        Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.95f),
                Color(0xFFF1F5F9).copy(alpha = 0.80f)
            )
        )
    }

    val borderBrush = if (accentBorder) {
        Brush.horizontalGradient(
            listOf(GamePalPrimary.copy(alpha = 0.8f), GamePalSecondary.copy(alpha = 0.6f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = if (isDark) 0.35f else 0.8f), Color.White.copy(alpha = if (isDark) 0.10f else 0.2f))
        )
    }

    return this
        .clip(shape)
        .background(backgroundBrush)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
}

@Composable
fun Modifier.auroraBackground(isDark: Boolean = true): Modifier {
    return this.drawBehind {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (isDark) {
            // Dark Frosted Glass obsidian backdrop (#0F1117) with luminous ambient orbs
            drawRect(color = Color(0xFF0F1117))

            // Upper left indigo bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x386366F1), Color.Transparent),
                    center = Offset(canvasWidth * 0.15f, canvasHeight * 0.08f),
                    radius = canvasWidth * 0.75f
                ),
                radius = canvasWidth * 0.75f,
                center = Offset(canvasWidth * 0.15f, canvasHeight * 0.08f)
            )

            // Right side cyan flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2E06B6D4), Color.Transparent),
                    center = Offset(canvasWidth * 0.90f, canvasHeight * 0.32f),
                    radius = canvasWidth * 0.65f
                ),
                radius = canvasWidth * 0.65f,
                center = Offset(canvasWidth * 0.90f, canvasHeight * 0.32f)
            )

            // Lower violet glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x288B5CF6), Color.Transparent),
                    center = Offset(canvasWidth * 0.35f, canvasHeight * 0.78f),
                    radius = canvasWidth * 0.80f
                ),
                radius = canvasWidth * 0.80f,
                center = Offset(canvasWidth * 0.35f, canvasHeight * 0.78f)
            )
        } else {
            // Light frosted canvas with soft pastel glow
            drawRect(color = Color(0xFFF1F5F9))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x35C7D2FE), Color.Transparent),
                    center = Offset(canvasWidth * 0.2f, canvasHeight * 0.1f),
                    radius = canvasWidth * 0.8f
                ),
                radius = canvasWidth * 0.8f,
                center = Offset(canvasWidth * 0.2f, canvasHeight * 0.1f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28BAE6FD), Color.Transparent),
                    center = Offset(canvasWidth * 0.8f, canvasHeight * 0.4f),
                    radius = canvasWidth * 0.7f
                ),
                radius = canvasWidth * 0.7f,
                center = Offset(canvasWidth * 0.8f, canvasHeight * 0.4f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x25DDD6FE), Color.Transparent),
                    center = Offset(canvasWidth * 0.5f, canvasHeight * 0.85f),
                    radius = canvasWidth * 0.8f
                ),
                radius = canvasWidth * 0.8f,
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.85f)
            )
        }
    }
}

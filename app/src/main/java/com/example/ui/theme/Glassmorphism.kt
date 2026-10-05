package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    shape: Shape = RoundedCornerShape(12.dp),
    isDark: Boolean = true,
    elevation: Dp = if (isDark) 0.dp else 2.dp,
    accentBorder: Boolean = false,
    customTint: Color? = null
): Modifier {
    val backgroundBrush = if (isDark) {
        val baseColor = customTint ?: Color(0xFF131316)
        Brush.verticalGradient(
            colors = listOf(
                (customTint?.copy(alpha = 0.25f) ?: Color(0xFF1A1A1E)), // Subtle top specular shine
                (customTint?.copy(alpha = 0.15f) ?: Color(0xFF131316)), // Mid body
                Color(0xFF0D0D10)                                         // Bottom subtle depth
            )
        )
    } else {
        val baseColor = customTint ?: Color.White
        Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.95f),
                Color(0xFFF4F4F5).copy(alpha = 0.85f)
            )
        )
    }

    val borderBrush = if (accentBorder) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.70f),
                Color(0xFFA1A1AA).copy(alpha = 0.40f),
                Color.White.copy(alpha = if (isDark) 0.15f else 0.35f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.20f else 0.85f), // Crisp top hairline rim
                Color.White.copy(alpha = if (isDark) 0.06f else 0.25f)  // Soft bottom rim
            )
        )
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
    val backgroundBrush = if (isDark) {
        val baseColor = customTint ?: Color(0xFF1E1E23)
        Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.35f),
                baseColor.copy(alpha = 0.18f)
            )
        )
    } else {
        val baseColor = customTint ?: Color.White
        Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.95f),
                Color(0xFFF4F4F5).copy(alpha = 0.85f)
            )
        )
    }

    val borderBrush = if (accentBorder) {
        Brush.horizontalGradient(
            listOf(Color.White.copy(alpha = 0.8f), Color(0xFFA1A1AA).copy(alpha = 0.5f))
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.25f else 0.8f),
                Color.White.copy(alpha = if (isDark) 0.08f else 0.2f)
            )
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
            // Sleek, deep matte obsidian black canvas (#09090B) without neon saturation
            drawRect(color = Color(0xFF09090B))

            // Very subtle, elegant top ambient illumination (monochrome soft white light)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x0AFFFFFF), Color.Transparent),
                    center = Offset(canvasWidth * 0.5f, 0f),
                    radius = canvasWidth * 0.9f
                ),
                radius = canvasWidth * 0.9f,
                center = Offset(canvasWidth * 0.5f, 0f)
            )
        } else {
            // Light minimalist canvas
            drawRect(color = Color(0xFFF4F4F5))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x08000000), Color.Transparent),
                    center = Offset(canvasWidth * 0.5f, 0f),
                    radius = canvasWidth * 0.9f
                ),
                radius = canvasWidth * 0.9f,
                center = Offset(canvasWidth * 0.5f, 0f)
            )
        }
    }
}

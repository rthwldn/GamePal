package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// PlayPulse Material 3 Expressive - Pure OLED Black (#000000) Palette
// =========================================================================

// Pure OLED True Black Canvas
val OledBlack = Color(0xFF000000)                  // 100% True Black for OLED pixels off
val OledSurface = Color(0xFF0D0D12)                // Deepest expressive graphite surface
val OledSurfaceContainer = Color(0xFF13131A)       // Elevated card container
val OledSurfaceContainerHigh = Color(0xFF1A1A24)   // Secondary floating container
val OledSurfaceContainerHighest = Color(0xFF232330)// Highlight container
val OledBorderHairline = Color(0x28FFFFFF)         // 16% crisp hairline specular rim
val OledBorderSubtle = Color(0x14FFFFFF)           // 8% subtle border
val OledBorderHighlight = Color(0x40FFFFFF)        // 25% luminous accent border

// Expressive High-Contrast Monochrome & Vibrant Accents
val PulseWhite = Color(0xFFFFFFFF)                 // Pure crisp white
val PulsePlatinum = Color(0xFFF1F5F9)              // Slate 100
val PulseSilver = Color(0xFFCBD5E1)                // Slate 300
val PulseZinc400 = Color(0xFF94A3B8)               // Slate 400
val PulseZinc500 = Color(0xFF64748B)               // Slate 500
val PulseZinc700 = Color(0xFF334155)               // Slate 700
val PulseZinc800 = Color(0xFF1E293B)               // Slate 800

// Expressive Accent Pops (Glow against OLED Black)
val ExpressiveElectricIndigo = Color(0xFF6366F1)   // Electric Indigo
val ExpressiveCyanGlow = Color(0xFF38BDF8)         // Vibrant Sky/Cyan
val ExpressiveEmerald = Color(0xFF10B981)          // Mint Emerald (active play/completed)
val ExpressiveAmber = Color(0xFFF59E0B)            // Warm Amber (paused)
val ExpressiveRose = Color(0xFFF43F5E)             // Neon Rose (dropped/delete)
val ExpressiveGold = Color(0xFFFBBF24)             // Bright Gold (ratings)

// Theme Aliases
val GamePalPrimary = PulseWhite
val GamePalPrimaryVariant = PulsePlatinum
val GamePalSecondary = ExpressiveCyanGlow
val GamePalAccent = ExpressiveElectricIndigo
val GamePalEmerald = ExpressiveEmerald
val GamePalAmber = ExpressiveAmber
val GamePalRose = ExpressiveRose

val DarkBackground = OledBlack
val DarkSurface = OledSurface
val DarkGlassSurface = OledSurfaceContainer
val DarkGlassCard = OledSurfaceContainer
val DarkGlassCardHover = OledSurfaceContainerHigh
val DarkGlassBorder = OledBorderHairline
val DarkGlassBorderSubtle = OledBorderSubtle
val DarkGlassGlow = Color(0x206366F1)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightGlassSurface = Color(0xFFF1F5F9)
val LightGlassCard = Color(0xFFFFFFFF)
val LightGlassBorder = Color(0xFFE2E8F0)
val LightGlassGlow = Color(0x10000000)
val LightTextPrimary = Color(0xFF09090B)
val LightTextSecondary = Color(0xFF64748B)

val RatingGold = ExpressiveGold
val RatingGoldGlow = Color(0x40FBBF24)

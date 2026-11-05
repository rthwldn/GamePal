package com.example.model

import androidx.compose.ui.graphics.Color

data class PlatformInfo(
    val id: String,
    val name: String,
    val shortName: String,
    val generation: Generation,
    val brandColor: Color,
    val accentColor: Color
) {
    enum class Generation(val label: String) {
        CURRENT("Aktuální platformy")
    }

    companion object {
        // Pouze 6 povolených platforem dle požadavku:
        // PC, PS5, Xbox Series, Switch, Switch 2 a Ostatní
        val ALL_PLATFORMS = listOf(
            PlatformInfo("pc", "PC", "PC", Generation.CURRENT, Color(0xFF1B2838), Color(0xFF67E8F9)),
            PlatformInfo("ps5", "PlayStation 5", "PS5", Generation.CURRENT, Color(0xFF0070D1), Color(0xFF60A5FA)),
            PlatformInfo("xbox_series", "Xbox Series", "Xbox Series", Generation.CURRENT, Color(0xFF107C10), Color(0xFF4ADE80)),
            PlatformInfo("switch", "Nintendo Switch", "Switch", Generation.CURRENT, Color(0xFFE60012), Color(0xFFF87171)),
            PlatformInfo("switch_2", "Nintendo Switch 2", "Switch 2", Generation.CURRENT, Color(0xFFC7000B), Color(0xFFFB7185)),
            PlatformInfo("other", "Ostatní", "Ostatní", Generation.CURRENT, Color(0xFF3F3F46), Color(0xFFA1A1AA))
        )

        fun findByName(name: String): PlatformInfo {
            val clean = name.trim().lowercase()
            return when {
                clean == "pc" || clean.contains("steam") || clean.contains("windows") || clean.contains("gog") || clean.contains("epic") || clean.contains("deck") -> 
                    ALL_PLATFORMS[0] // PC
                clean.contains("ps5") || clean.contains("playstation 5") -> 
                    ALL_PLATFORMS[1] // PlayStation 5
                clean.contains("xbox series") || clean.contains("series x") || clean.contains("series s") -> 
                    ALL_PLATFORMS[2] // Xbox Series
                clean.contains("switch 2") || clean.contains("switch2") -> 
                    ALL_PLATFORMS[4] // Nintendo Switch 2
                clean.contains("switch") || clean.contains("nintendo switch") -> 
                    ALL_PLATFORMS[3] // Nintendo Switch
                clean == "ostatní" || clean == "ostatni" || clean == "other" ->
                    ALL_PLATFORMS[5] // Ostatní
                else -> {
                    // Check direct match or map legacy platforms to Ostatní
                    ALL_PLATFORMS.firstOrNull {
                        it.name.equals(name, ignoreCase = true) ||
                        it.shortName.equals(name, ignoreCase = true) ||
                        it.id.equals(name, ignoreCase = true)
                    } ?: ALL_PLATFORMS[5] // Ostatní
                }
            }
        }
    }
}

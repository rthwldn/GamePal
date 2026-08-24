package com.example.model

import androidx.compose.ui.graphics.Color

enum class GameStatus(
    val id: String,
    val titleCs: String,
    val titleEn: String,
    val primaryColor: Color,
    val accentColor: Color
) {
    PLAYING(
        "PLAYING",
        "Právě hraji",
        "Playing",
        Color(0xFF06B6D4), // Cyan
        Color(0xFF22D3EE)
    ),
    COMPLETED(
        "COMPLETED",
        "Dohráno",
        "Completed",
        Color(0xFF10B981), // Emerald
        Color(0xFF34D399)
    ),
    BACKLOG(
        "BACKLOG",
        "V knihovně",
        "Backlog",
        Color(0xFF6366F1), // Indigo
        Color(0xFF818CF8)
    ),
    PAUSED(
        "PAUSED",
        "Pozastaveno",
        "Paused",
        Color(0xFFF59E0B), // Amber
        Color(0xFFFBBF24)
    ),
    DROPPED(
        "DROPPED",
        "Odloženo",
        "Dropped",
        Color(0xFFEF4444), // Rose/Red
        Color(0xFFF87171)
    );

    companion object {
        fun fromString(value: String): GameStatus {
            return entries.firstOrNull { it.id.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: PLAYING
        }
    }
}

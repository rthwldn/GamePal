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
        CURRENT("Aktuální & Next-Gen"),
        LAST_GEN("Předchozí generace"),
        SEVENTH_GEN("Éra HD (PS3/X360/Wii)"),
        RETRO_CLASSICS("Retro klasiky"),
        HANDHELD("Handheldy & Přenosné"),
        PC_OTHER("PC & Ostatní")
    }

    companion object {
        val ALL_PLATFORMS = listOf(
            // Current & Modern
            PlatformInfo("ps5", "PlayStation 5", "PS5", Generation.CURRENT, Color(0xFF0070D1), Color(0xFF60A5FA)),
            PlatformInfo("xbox_series", "Xbox Series X|S", "Xbox Series", Generation.CURRENT, Color(0xFF107C10), Color(0xFF4ADE80)),
            PlatformInfo("switch", "Nintendo Switch", "Switch", Generation.CURRENT, Color(0xFFE60012), Color(0xFFF87171)),
            PlatformInfo("steam_deck", "Steam Deck", "Deck", Generation.CURRENT, Color(0xFF1A9FFF), Color(0xFF38BDF8)),
            PlatformInfo("pc", "PC (Steam / GOG / Epic)", "PC", Generation.PC_OTHER, Color(0xFF1B2838), Color(0xFF67E8F9)),

            // Last Gen
            PlatformInfo("ps4", "PlayStation 4", "PS4", Generation.LAST_GEN, Color(0xFF003791), Color(0xFF3B82F6)),
            PlatformInfo("xbox_one", "Xbox One", "Xbox One", Generation.LAST_GEN, Color(0xFF0E7A0D), Color(0xFF22C55E)),
            PlatformInfo("wii_u", "Nintendo Wii U", "Wii U", Generation.LAST_GEN, Color(0xFF009AC7), Color(0xFF38BDF8)),

            // 7th Gen
            PlatformInfo("ps3", "PlayStation 3", "PS3", Generation.SEVENTH_GEN, Color(0xFF1E293B), Color(0xFF94A3B8)),
            PlatformInfo("xbox_360", "Xbox 360", "Xbox 360", Generation.SEVENTH_GEN, Color(0xFF65A30D), Color(0xFFA3E635)),
            PlatformInfo("wii", "Nintendo Wii", "Wii", Generation.SEVENTH_GEN, Color(0xFF0284C7), Color(0xFF7DD3FC)),

            // Handhelds
            PlatformInfo("ps_vita", "PlayStation Vita", "PS Vita", Generation.HANDHELD, Color(0xFF0284C7), Color(0xFF38BDF8)),
            PlatformInfo("psp", "PlayStation Portable", "PSP", Generation.HANDHELD, Color(0xFF334155), Color(0xFF94A3B8)),
            PlatformInfo("n3ds", "Nintendo 3DS / 2DS", "3DS", Generation.HANDHELD, Color(0xFFDC2626), Color(0xFFFCA5A5)),
            PlatformInfo("nds", "Nintendo DS", "NDS", Generation.HANDHELD, Color(0xFF64748B), Color(0xFFCBD5E1)),
            PlatformInfo("gba", "Game Boy Advance", "GBA", Generation.HANDHELD, Color(0xFF7C3AED), Color(0xFFC084FC)),
            PlatformInfo("gbc", "Game Boy / Color", "GBC", Generation.HANDHELD, Color(0xFFCA8A04), Color(0xFFFDE047)),

            // Retro Classics
            PlatformInfo("ps2", "PlayStation 2", "PS2", Generation.RETRO_CLASSICS, Color(0xFF1E1B4B), Color(0xFF818CF8)),
            PlatformInfo("ps1", "PlayStation 1 (PSX)", "PS1", Generation.RETRO_CLASSICS, Color(0xFF475569), Color(0xFF94A3B8)),
            PlatformInfo("gamecube", "Nintendo GameCube", "GameCube", Generation.RETRO_CLASSICS, Color(0xFF4338CA), Color(0xFFA5B4FC)),
            PlatformInfo("n64", "Nintendo 64", "N64", Generation.RETRO_CLASSICS, Color(0xFFDC2626), Color(0xFFF87171)),
            PlatformInfo("snes", "Super Nintendo (SNES)", "SNES", Generation.RETRO_CLASSICS, Color(0xFF6D28D9), Color(0xFFDDD6FE)),
            PlatformInfo("nes", "Nintendo Entertainment System (NES)", "NES", Generation.RETRO_CLASSICS, Color(0xFFB91C1C), Color(0xFFFCA5A5)),
            PlatformInfo("genesis", "Sega Genesis / Mega Drive", "Genesis", Generation.RETRO_CLASSICS, Color(0xFF0F172A), Color(0xFF64748B)),
            PlatformInfo("dreamcast", "Sega Dreamcast", "Dreamcast", Generation.RETRO_CLASSICS, Color(0xFFEA580C), Color(0xFFFDBA74)),
            PlatformInfo("xbox_classic", "Original Xbox", "Xbox", Generation.RETRO_CLASSICS, Color(0xFF15803D), Color(0xFF86EFAC)),

            // Other
            PlatformInfo("mobile", "Mobile (iOS / Android)", "Mobile", Generation.PC_OTHER, Color(0xFF059669), Color(0xFF6EE7B7)),
            PlatformInfo("vr", "Meta Quest / VR", "VR", Generation.PC_OTHER, Color(0xFF9333EA), Color(0xFFD8B4FE))
        )

        fun findByName(name: String): PlatformInfo {
            return ALL_PLATFORMS.firstOrNull { 
                it.name.equals(name, ignoreCase = true) || 
                it.shortName.equals(name, ignoreCase = true) ||
                it.id.equals(name, ignoreCase = true)
            } ?: PlatformInfo("other", name, name, Generation.PC_OTHER, Color(0xFF4F46E5), Color(0xFF818CF8))
        }
    }
}

package com.example

import com.example.model.GameEntity
import com.example.model.PlaySessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGameEntityTimeFormatting() {
        val game1 = GameEntity(
            title = "Test Game",
            platform = "PS5",
            coverUrl = "",
            totalPlayTimeSeconds = 3665L // 1h 1m 5s
        )
        assertEquals("1h 1m", game1.formattedTotalTime)
        assertEquals("1:01:05", game1.detailedFormattedTime)

        val game2 = GameEntity(
            title = "Short Game",
            platform = "Switch",
            coverUrl = "",
            totalPlayTimeSeconds = 125L // 2m 5s
        )
        assertEquals("2m", game2.formattedTotalTime)
        assertEquals("02:05", game2.detailedFormattedTime)
    }

    @Test
    fun testPlaySessionFormatting() {
        val session1 = PlaySessionEntity(
            gameId = 1L,
            gameTitle = "Zelda",
            platform = "Switch",
            startTime = 1000L,
            endTime = 5000L,
            durationSeconds = 3720L // 1h 2m
        )
        assertEquals("1h 2m", session1.formattedDuration)

        val session2 = PlaySessionEntity(
            gameId = 2L,
            gameTitle = "Hollow Knight",
            platform = "PC",
            startTime = 1000L,
            endTime = 2000L,
            durationSeconds = 45L
        )
        assertEquals("45s", session2.formattedDuration)
    }

    @Test
    fun testActiveGameSessionFormatting() {
        val activeSession = com.example.service.ActiveGameSession(
            gameId = 1L,
            gameTitle = "Cyberpunk 2077",
            platform = "PC",
            coverUrl = "",
            elapsedSeconds = 7325L // 2h 2m 5s
        )
        assertEquals("02:02:05", activeSession.formattedTime)

        val shortSession = com.example.service.ActiveGameSession(
            gameId = 1L,
            gameTitle = "Tetris",
            platform = "Game Boy",
            coverUrl = "",
            elapsedSeconds = 85L // 1m 25s
        )
        assertEquals("01:25", shortSession.formattedTime)
    }
}


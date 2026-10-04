package com.example.data

import com.example.GamePalApplication
import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.model.PlaySessionEntity
import com.example.widget.GameTrackerWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameDao: GameDao,
    private val playSessionDao: PlaySessionDao
) {
    private fun notifyWidget() {
        try {
            GameTrackerWidgetProvider.updateAllWidgets(GamePalApplication.instance)
        } catch (_: Exception) {}
    }

    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    val allSessions: Flow<List<PlaySessionEntity>> = playSessionDao.getAllSessions()
    val totalLibraryPlayTime: Flow<Long?> = gameDao.getTotalLibraryPlayTime()

    fun getGameById(id: Long): Flow<GameEntity?> = gameDao.getGameById(id)

    suspend fun getGameByIdDirect(id: Long): GameEntity? = withContext(Dispatchers.IO) {
        gameDao.getGameByIdDirect(id)
    }

    fun getSessionsForGame(gameId: Long): Flow<List<PlaySessionEntity>> =
        playSessionDao.getSessionsForGame(gameId)

    suspend fun insertGame(game: GameEntity): Long = withContext(Dispatchers.IO) {
        val id = gameDao.insertGame(game)
        notifyWidget()
        id
    }

    suspend fun updateGame(game: GameEntity) = withContext(Dispatchers.IO) {
        gameDao.updateGame(game)
        notifyWidget()
    }

    suspend fun deleteGame(id: Long) = withContext(Dispatchers.IO) {
        gameDao.deleteGameById(id)
        notifyWidget()
    }

    suspend fun updateRatingAndReview(id: Long, rating: Int?, review: String, notes: String) =
        withContext(Dispatchers.IO) {
            gameDao.updateRatingAndReview(id, rating, review, notes)
            notifyWidget()
        }

    suspend fun updateStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        gameDao.updateStatus(id, status)
        notifyWidget()
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        gameDao.updateFavorite(id, isFavorite)
        notifyWidget()
    }

    suspend fun addPlayTime(gameId: Long, durationSeconds: Long, timestamp: Long = System.currentTimeMillis()) =
        withContext(Dispatchers.IO) {
            gameDao.addPlayTime(gameId, durationSeconds, timestamp)
            notifyWidget()
        }

    suspend fun recordSession(
        gameId: Long,
        durationSeconds: Long,
        startTime: Long,
        endTime: Long = System.currentTimeMillis(),
        sessionNotes: String = ""
    ) = withContext(Dispatchers.IO) {
        val game = gameDao.getGameByIdDirect(gameId)
        if (game != null) {
            // 1. Insert session record
            val session = PlaySessionEntity(
                gameId = gameId,
                gameTitle = game.title,
                platform = game.platform,
                startTime = startTime,
                endTime = endTime,
                durationSeconds = durationSeconds,
                notes = sessionNotes
            )
            playSessionDao.insertSession(session)

            // 2. Increment total play time and update last played
            gameDao.addPlayTime(gameId, durationSeconds, endTime)
            notifyWidget()
        }
    }

    suspend fun seedInitialDataIfNeeded() {
        // Clean start: No preloaded games, users add their own games
    }
}

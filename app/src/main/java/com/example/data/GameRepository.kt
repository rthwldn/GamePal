package com.example.data

import com.example.GamePalApplication
import com.example.data.firestore.FirestoreSyncManager
import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.model.PlaySessionEntity
import com.example.widget.GameTrackerWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameDao: GameDao,
    private val playSessionDao: PlaySessionDao,
    var syncManager: FirestoreSyncManager? = null
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
        val savedGame = game.copy(id = id)
        syncManager?.syncGame(savedGame)
        id
    }

    suspend fun updateGame(game: GameEntity) = withContext(Dispatchers.IO) {
        gameDao.updateGame(game)
        notifyWidget()
        syncManager?.syncGame(game)
    }

    suspend fun deleteGame(id: Long) = withContext(Dispatchers.IO) {
        gameDao.deleteGameById(id)
        notifyWidget()
        syncManager?.deleteGameFromCloud(id)
    }

    suspend fun updateRatingAndReview(id: Long, rating: Int?, review: String, notes: String) =
        withContext(Dispatchers.IO) {
            gameDao.updateRatingAndReview(id, rating, review, notes)
            notifyWidget()
            gameDao.getGameByIdDirect(id)?.let { syncManager?.syncGame(it) }
        }

    suspend fun updateStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        gameDao.updateStatus(id, status)
        notifyWidget()
        gameDao.getGameByIdDirect(id)?.let { syncManager?.syncGame(it) }
    }

    suspend fun updateCoverUrl(id: Long, coverUrl: String) = withContext(Dispatchers.IO) {
        gameDao.updateCoverUrl(id, coverUrl)
        notifyWidget()
        gameDao.getGameByIdDirect(id)?.let { syncManager?.syncGame(it) }
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        gameDao.updateFavorite(id, isFavorite)
        notifyWidget()
        gameDao.getGameByIdDirect(id)?.let { syncManager?.syncGame(it) }
    }

    suspend fun addPlayTime(gameId: Long, durationSeconds: Long, timestamp: Long = System.currentTimeMillis()) =
        withContext(Dispatchers.IO) {
            gameDao.addPlayTime(gameId, durationSeconds, timestamp)
            notifyWidget()
            gameDao.getGameByIdDirect(gameId)?.let { syncManager?.syncGame(it) }
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
            val sessionId = playSessionDao.insertSession(session)

            // 2. Increment total play time and update last played
            gameDao.addPlayTime(gameId, durationSeconds, endTime)
            notifyWidget()

            // 3. Sync to Firestore
            val sessionWithId = session.copy(id = sessionId)
            syncManager?.syncSession(sessionWithId)
            gameDao.getGameByIdDirect(gameId)?.let { syncManager?.syncGame(it) }
        }
    }

    suspend fun seedInitialDataIfNeeded() {
        // Clean start: No preloaded games, users add their own games
    }
}

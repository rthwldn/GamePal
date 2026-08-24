package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY lastPlayedTimestamp DESC, addedTimestamp DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameById(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameByIdDirect(id: Long): GameEntity?

    @Query("SELECT * FROM games WHERE status = :status ORDER BY lastPlayedTimestamp DESC, addedTimestamp DESC")
    fun getGamesByStatus(status: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE platform = :platform ORDER BY lastPlayedTimestamp DESC, addedTimestamp DESC")
    fun getGamesByPlatform(platform: String): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("UPDATE games SET totalPlayTimeSeconds = totalPlayTimeSeconds + :addedSeconds, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun addPlayTime(id: Long, addedSeconds: Long, timestamp: Long)

    @Query("UPDATE games SET rating = :rating, review = :review, notes = :notes WHERE id = :id")
    suspend fun updateRatingAndReview(id: Long, rating: Int?, review: String, notes: String)

    @Query("UPDATE games SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE games SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGamesCount(): Int

    @Query("SELECT SUM(totalPlayTimeSeconds) FROM games")
    fun getTotalLibraryPlayTime(): Flow<Long?>
}

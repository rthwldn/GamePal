package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.PlaySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaySessionDao {
    @Query("SELECT * FROM play_sessions ORDER BY endTime DESC")
    fun getAllSessions(): Flow<List<PlaySessionEntity>>

    @Query("SELECT * FROM play_sessions WHERE gameId = :gameId ORDER BY endTime DESC")
    fun getSessionsForGame(gameId: Long): Flow<List<PlaySessionEntity>>

    @Query("SELECT * FROM play_sessions ORDER BY endTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<PlaySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PlaySessionEntity): Long

    @Delete
    suspend fun deleteSession(session: PlaySessionEntity)

    @Query("DELETE FROM play_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT SUM(durationSeconds) FROM play_sessions WHERE gameId = :gameId")
    suspend fun getTotalDurationForGame(gameId: Long): Long?

    @Query("SELECT * FROM play_sessions WHERE endTime >= :sinceTimestamp ORDER BY endTime DESC")
    suspend fun getSessionsSince(sinceTimestamp: Long): List<PlaySessionEntity>

    @Query("SELECT * FROM play_sessions ORDER BY endTime DESC LIMIT 1")
    suspend fun getLastSession(): PlaySessionEntity?
}

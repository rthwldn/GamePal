package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val platform: String,
    val coverUrl: String,
    val description: String = "",
    val releaseYear: Int = 0,
    val genres: String = "",
    val developer: String = "",
    val status: String = GameStatus.PLAYING.id,
    val rating: Int? = null, // 1 to 10 scale
    val review: String = "",
    val notes: String = "",
    val totalPlayTimeSeconds: Long = 0L,
    val lastPlayedTimestamp: Long? = null,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    val formattedTotalTime: String
        get() {
            val hours = totalPlayTimeSeconds / 3600
            val minutes = (totalPlayTimeSeconds % 3600) / 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }

    val detailedFormattedTime: String
        get() {
            val hours = totalPlayTimeSeconds / 3600
            val minutes = (totalPlayTimeSeconds % 3600) / 60
            val seconds = totalPlayTimeSeconds % 60
            return if (hours > 0) {
                String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
}

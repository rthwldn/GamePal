package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    val userId: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val lastSyncTimestamp: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreGame(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val platform: String = "",
    val coverUrl: String? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val genres: String? = null,
    val developer: String? = null,
    val status: String = "playing",
    val rating: Int? = null,
    val review: String? = null,
    val notes: String? = null,
    val totalPlayTimeSeconds: Long = 0L,
    val lastPlayedTimestamp: Long? = null,
    val addedTimestamp: Long = 0L,
    val isFavorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestorePlaySession(
    val id: String = "",
    val userId: String = "",
    val gameId: String = "",
    val gameTitle: String? = null,
    val platform: String? = null,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val durationSeconds: Long = 0L,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

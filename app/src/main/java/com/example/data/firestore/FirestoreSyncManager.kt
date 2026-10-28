package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.GameDao
import com.example.data.PlaySessionDao
import com.example.model.GameEntity
import com.example.model.PlaySessionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val lastSyncedTime: Long, val message: String = "Záloha je aktuální") : SyncState()
    data class Error(val message: String) : SyncState()
}

class FirestoreSyncManager(
    private val context: Context,
    private val gameDao: GameDao,
    private val playSessionDao: PlaySessionDao
) {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun syncAll(): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: run {
            _syncState.value = SyncState.Idle
            return@withContext false
        }

        _syncState.value = SyncState.Syncing

        try {
            val userDocRef = db.collection("users").document(uid)
            val gamesCollectionRef = userDocRef.collection("games")
            val sessionsCollectionRef = userDocRef.collection("sessions")

            // 1. Ensure User profile exists or update sync time
            val userProfilePayload = mapOf(
                "userId" to uid,
                "email" to auth.currentUser?.email,
                "displayName" to auth.currentUser?.displayName,
                "lastSyncTimestamp" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val userDocSnap = userDocRef.get().await()
            if (!userDocSnap.exists()) {
                userDocRef.set(
                    userProfilePayload + mapOf("createdAt" to FieldValue.serverTimestamp()),
                    SetOptions.merge()
                ).await()
            } else {
                userDocRef.set(userProfilePayload, SetOptions.merge()).await()
            }

            // 2. Fetch remote games
            val remoteGamesSnap = gamesCollectionRef.get().await()
            val remoteGamesMap = mutableMapOf<String, FirestoreGame>()
            for (doc in remoteGamesSnap.documents) {
                doc.toObject(FirestoreGame::class.java)?.let { game ->
                    val effective = if (game.id.isBlank()) game.copy(id = doc.id) else game
                    remoteGamesMap[doc.id] = effective
                }
            }

            // 3. Local games
            val localGames = gameDao.getAllGamesDirect()
            val localGamesByDocId = localGames.associateBy { "game_${it.id}" }

            // Restore from remote to local
            for ((docId, remoteGame) in remoteGamesMap) {
                val localGame = localGamesByDocId[docId]
                if (localGame == null) {
                    // Game is in cloud but not local (e.g. new phone) -> Restore!
                    val idLong = docId.removePrefix("game_").toLongOrNull() ?: 0L
                    val restored = GameEntity(
                        id = idLong,
                        title = remoteGame.title,
                        platform = remoteGame.platform,
                        coverUrl = remoteGame.coverUrl ?: "",
                        description = remoteGame.description ?: "",
                        releaseYear = remoteGame.releaseYear ?: 0,
                        genres = remoteGame.genres ?: "",
                        developer = remoteGame.developer ?: "",
                        status = remoteGame.status,
                        rating = remoteGame.rating,
                        review = remoteGame.review ?: "",
                        notes = remoteGame.notes ?: "",
                        totalPlayTimeSeconds = remoteGame.totalPlayTimeSeconds,
                        lastPlayedTimestamp = remoteGame.lastPlayedTimestamp,
                        addedTimestamp = if (remoteGame.addedTimestamp > 0) remoteGame.addedTimestamp else System.currentTimeMillis(),
                        isFavorite = remoteGame.isFavorite
                    )
                    gameDao.insertGame(restored)
                } else {
                    // Both exist: if remote has more playtime or newer updates, merge
                    if (remoteGame.totalPlayTimeSeconds > localGame.totalPlayTimeSeconds ||
                        (remoteGame.lastPlayedTimestamp ?: 0L) > (localGame.lastPlayedTimestamp ?: 0L)
                    ) {
                        val updated = localGame.copy(
                            totalPlayTimeSeconds = maxOf(localGame.totalPlayTimeSeconds, remoteGame.totalPlayTimeSeconds),
                            lastPlayedTimestamp = maxOf(localGame.lastPlayedTimestamp ?: 0L, remoteGame.lastPlayedTimestamp ?: 0L).takeIf { it > 0 },
                            rating = remoteGame.rating ?: localGame.rating,
                            review = if (remoteGame.review.isNullOrBlank()) localGame.review else remoteGame.review,
                            status = remoteGame.status
                        )
                        gameDao.updateGame(updated)
                    }
                }
            }

            // Upload local games to remote if not present or newer
            val updatedLocalGames = gameDao.getAllGamesDirect()
            for (game in updatedLocalGames) {
                val docId = "game_${game.id}"
                val remoteGame = remoteGamesMap[docId]
                if (remoteGame == null ||
                    game.totalPlayTimeSeconds > remoteGame.totalPlayTimeSeconds ||
                    (game.lastPlayedTimestamp ?: 0L) > (remoteGame.lastPlayedTimestamp ?: 0L)
                ) {
                    val payload = mapOf(
                        "id" to docId,
                        "userId" to uid,
                        "title" to game.title,
                        "platform" to game.platform,
                        "coverUrl" to game.coverUrl,
                        "description" to game.description,
                        "releaseYear" to game.releaseYear,
                        "genres" to game.genres,
                        "developer" to game.developer,
                        "status" to game.status,
                        "rating" to game.rating,
                        "review" to game.review,
                        "notes" to game.notes,
                        "totalPlayTimeSeconds" to game.totalPlayTimeSeconds,
                        "lastPlayedTimestamp" to game.lastPlayedTimestamp,
                        "addedTimestamp" to game.addedTimestamp,
                        "isFavorite" to game.isFavorite,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }

                    val docRef = gamesCollectionRef.document(docId)
                    val snap = docRef.get().await()
                    if (!snap.exists()) {
                        docRef.set(payload + mapOf("createdAt" to FieldValue.serverTimestamp())).await()
                    } else {
                        docRef.set(payload, SetOptions.merge()).await()
                    }
                }
            }

            // 4. Synchronize PlaySessions
            val remoteSessionsSnap = sessionsCollectionRef.get().await()
            val remoteSessionsMap = mutableMapOf<String, FirestorePlaySession>()
            for (doc in remoteSessionsSnap.documents) {
                doc.toObject(FirestorePlaySession::class.java)?.let { session ->
                    val effective = if (session.id.isBlank()) session.copy(id = doc.id) else session
                    remoteSessionsMap[doc.id] = effective
                }
            }

            val localSessions = playSessionDao.getAllSessionsDirect()
            val localSessionsByDocId = localSessions.associateBy { "session_${it.id}" }

            for ((docId, remoteSession) in remoteSessionsMap) {
                if (!localSessionsByDocId.containsKey(docId)) {
                    val idLong = docId.removePrefix("session_").toLongOrNull() ?: 0L
                    val gameIdLong = remoteSession.gameId.removePrefix("game_").toLongOrNull() ?: 0L
                    val restoredSession = PlaySessionEntity(
                        id = idLong,
                        gameId = gameIdLong,
                        gameTitle = remoteSession.gameTitle ?: "",
                        platform = remoteSession.platform ?: "",
                        startTime = remoteSession.startTime,
                        endTime = remoteSession.endTime,
                        durationSeconds = remoteSession.durationSeconds,
                        notes = remoteSession.notes ?: ""
                    )
                    playSessionDao.insertSession(restoredSession)
                }
            }

            // Upload missing local sessions to remote
            for (session in localSessions) {
                val docId = "session_${session.id}"
                if (!remoteSessionsMap.containsKey(docId)) {
                    val payload = mapOf(
                        "id" to docId,
                        "userId" to uid,
                        "gameId" to "game_${session.gameId}",
                        "gameTitle" to session.gameTitle,
                        "platform" to session.platform,
                        "startTime" to session.startTime,
                        "endTime" to session.endTime,
                        "durationSeconds" to session.durationSeconds,
                        "notes" to session.notes,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }

                    sessionsCollectionRef.document(docId).set(payload).await()
                }
            }

            _syncState.value = SyncState.Success(System.currentTimeMillis())
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            _syncState.value = SyncState.Error(e.localizedMessage ?: "Chyba synchronizace")
            false
        }
    }

    suspend fun syncGame(game: GameEntity) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val docId = "game_${game.id}"
        val docRef = db.collection("users").document(uid).collection("games").document(docId)

        val payload = mapOf(
            "id" to docId,
            "userId" to uid,
            "title" to game.title,
            "platform" to game.platform,
            "coverUrl" to game.coverUrl,
            "description" to game.description,
            "releaseYear" to game.releaseYear,
            "genres" to game.genres,
            "developer" to game.developer,
            "status" to game.status,
            "rating" to game.rating,
            "review" to game.review,
            "notes" to game.notes,
            "totalPlayTimeSeconds" to game.totalPlayTimeSeconds,
            "lastPlayedTimestamp" to game.lastPlayedTimestamp,
            "addedTimestamp" to game.addedTimestamp,
            "isFavorite" to game.isFavorite,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(payload + mapOf("createdAt" to FieldValue.serverTimestamp())).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun deleteGameFromCloud(gameId: Long) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val docId = "game_$gameId"
        val docRef = db.collection("users").document(uid).collection("games").document(docId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    suspend fun syncSession(session: PlaySessionEntity) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val docId = "session_${session.id}"
        val docRef = db.collection("users").document(uid).collection("sessions").document(docId)

        val payload = mapOf(
            "id" to docId,
            "userId" to uid,
            "gameId" to "game_${session.gameId}",
            "gameTitle" to session.gameTitle,
            "platform" to session.platform,
            "startTime" to session.startTime,
            "endTime" to session.endTime,
            "durationSeconds" to session.durationSeconds,
            "notes" to session.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }
}

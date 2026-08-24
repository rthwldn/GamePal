package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameMetadataService
import com.example.data.GameRepository
import com.example.data.GameSearchResult
import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.model.PlaySessionEntity
import com.example.service.ActiveGameSession
import com.example.service.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    LAST_PLAYED("Naposledy hráno"),
    PLAY_TIME("Čas hraní"),
    RATING("Hodnocení"),
    TITLE("Název A-Z"),
    DATE_ADDED("Nedávno přidáno")
}

data class LibraryStats(
    val totalGames: Int = 0,
    val completedGames: Int = 0,
    val playingGames: Int = 0,
    val backlogGames: Int = 0,
    val totalPlaySeconds: Long = 0L,
    val averageRating: Double = 0.0,
    val favoritePlatform: String = "N/A"
) {
    val totalHours: String
        get() {
            val hours = totalPlaySeconds / 3600
            val minutes = (totalPlaySeconds % 3600) / 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class GamePalViewModel(
    private val repository: GameRepository,
    private val metadataService: GameMetadataService
) : ViewModel() {

    val allGames: StateFlow<List<GameEntity>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<PlaySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<ActiveGameSession?> = SessionManager.activeSession

    // Filtering & Sorting
    val selectedStatusFilter = MutableStateFlow<String?>(null) // null = ALL
    val selectedPlatformFilter = MutableStateFlow<String?>(null) // null = ALL
    val searchQuery = MutableStateFlow("")
    val sortOption = MutableStateFlow(SortOption.LAST_PLAYED)

    // Selected Game for Detail View
    private val _selectedGameId = MutableStateFlow<Long?>(null)
    val selectedGameId: StateFlow<Long?> = _selectedGameId.asStateFlow()

    val selectedGame: StateFlow<GameEntity?> = _selectedGameId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getGameById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedGameSessions: StateFlow<List<PlaySessionEntity>> = _selectedGameId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getSessionsForGame(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Games Flow
    val filteredGames: StateFlow<List<GameEntity>> = combine(
        allGames,
        selectedStatusFilter,
        selectedPlatformFilter,
        searchQuery,
        sortOption
    ) { games, statusFilter, platformFilter, query, sort ->
        var list = games

        if (!statusFilter.isNullOrBlank() && statusFilter != "ALL") {
            list = list.filter { it.status.equals(statusFilter, ignoreCase = true) }
        }

        if (!platformFilter.isNullOrBlank() && platformFilter != "ALL") {
            list = list.filter { it.platform.equals(platformFilter, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim()
            list = list.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.platform.contains(q, ignoreCase = true) ||
                it.developer.contains(q, ignoreCase = true) ||
                it.genres.contains(q, ignoreCase = true)
            }
        }

        when (sort) {
            SortOption.LAST_PLAYED -> list.sortedWith(
                compareByDescending<GameEntity> { it.lastPlayedTimestamp ?: 0L }
                    .thenByDescending { it.addedTimestamp }
            )
            SortOption.PLAY_TIME -> list.sortedByDescending { it.totalPlayTimeSeconds }
            SortOption.RATING -> list.sortedWith(
                compareByDescending<GameEntity> { it.rating ?: -1 }
                    .thenByDescending { it.totalPlayTimeSeconds }
            )
            SortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            SortOption.DATE_ADDED -> list.sortedByDescending { it.addedTimestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Library Statistics
    val stats: StateFlow<LibraryStats> = allGames.combine(allSessions) { games, sessions ->
        if (games.isEmpty()) {
            LibraryStats()
        } else {
            val totalPlaySec = games.sumOf { it.totalPlayTimeSeconds }
            val completed = games.count { it.status == GameStatus.COMPLETED.id }
            val playing = games.count { it.status == GameStatus.PLAYING.id }
            val backlog = games.count { it.status == GameStatus.BACKLOG.id }
            val ratedGames = games.mapNotNull { it.rating }
            val avgRating = if (ratedGames.isNotEmpty()) ratedGames.average() else 0.0

            val topPlatform = games.groupBy { it.platform }
                .maxByOrNull { it.value.size }?.key ?: "N/A"

            LibraryStats(
                totalGames = games.size,
                completedGames = completed,
                playingGames = playing,
                backlogGames = backlog,
                totalPlaySeconds = totalPlaySec,
                averageRating = avgRating,
                favoritePlatform = topPlatform
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryStats())

    // Search for Adding Games
    val addGameSearchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<GameSearchResult>>(emptyList())
    val searchResults: StateFlow<List<GameSearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        // Debounced search for add game dialog
        viewModelScope.launch {
            addGameSearchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    _isSearching.value = true
                    try {
                        val results = metadataService.searchGames(query)
                        _searchResults.value = results
                    } catch (_: Exception) {
                        _searchResults.value = emptyList()
                    } finally {
                        _isSearching.value = false
                    }
                }
        }
    }

    // Detail Selection
    fun selectGame(gameId: Long?) {
        _selectedGameId.value = gameId
    }

    // Session Management (Live Activity)
    fun startSession(context: Context, game: GameEntity) {
        SessionManager.startSession(context, game)
    }

    fun pauseSession(context: Context) {
        SessionManager.pauseSession(context)
    }

    fun resumeSession(context: Context) {
        SessionManager.resumeSession(context)
    }

    fun addMinutesToActiveSession(minutes: Int) {
        SessionManager.addMinutes(minutes)
    }

    fun stopSession(context: Context, sessionNotes: String = "") {
        val finished = SessionManager.stopSession(context)
        if (finished != null && finished.elapsedSeconds > 0) {
            viewModelScope.launch {
                repository.recordSession(
                    gameId = finished.gameId,
                    durationSeconds = finished.elapsedSeconds,
                    startTime = finished.startTimestamp,
                    endTime = System.currentTimeMillis(),
                    sessionNotes = sessionNotes
                )
            }
        }
    }

    // CRUD & Game Editing
    fun addGame(
        title: String,
        platform: String,
        coverUrl: String,
        description: String,
        releaseYear: Int,
        genres: String,
        developer: String,
        status: String = GameStatus.PLAYING.id,
        rating: Int? = null,
        review: String = "",
        notes: String = "",
        initialPlayTimeMinutes: Long = 0L
    ) {
        viewModelScope.launch {
            val newGame = GameEntity(
                title = title.trim(),
                platform = platform,
                coverUrl = coverUrl.trim(),
                description = description.trim(),
                releaseYear = releaseYear,
                genres = genres.trim(),
                developer = developer.trim(),
                status = status,
                rating = rating,
                review = review.trim(),
                notes = notes.trim(),
                totalPlayTimeSeconds = initialPlayTimeMinutes * 60,
                lastPlayedTimestamp = if (initialPlayTimeMinutes > 0) System.currentTimeMillis() else null,
                addedTimestamp = System.currentTimeMillis()
            )
            repository.insertGame(newGame)
        }
    }

    fun updateGame(game: GameEntity) {
        viewModelScope.launch {
            repository.updateGame(game)
        }
    }

    fun updateRatingAndReview(gameId: Long, rating: Int?, review: String, notes: String) {
        viewModelScope.launch {
            repository.updateRatingAndReview(gameId, rating, review, notes)
        }
    }

    fun updateGameStatus(gameId: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(gameId, status)
        }
    }

    fun toggleFavorite(gameId: Long) {
        val game = allGames.value.find { it.id == gameId } ?: return
        viewModelScope.launch {
            repository.updateFavorite(gameId, !game.isFavorite)
        }
    }

    fun addManualTime(gameId: Long, minutes: Long) {
        viewModelScope.launch {
            repository.addPlayTime(gameId, minutes * 60)
        }
    }

    fun deleteGame(gameId: Long) {
        if (SessionManager.activeSession.value?.gameId == gameId) {
            try {
                SessionManager.cancelSession(com.example.GamePalApplication.instance)
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            repository.deleteGame(gameId)
            if (_selectedGameId.value == gameId) {
                _selectedGameId.value = null
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: GameRepository,
            metadataService: GameMetadataService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GamePalViewModel(repository, metadataService) as T
            }
        }
    }
}

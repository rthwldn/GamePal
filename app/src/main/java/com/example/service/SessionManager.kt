package com.example.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock
import com.example.model.GameEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class ActiveGameSession(
    val gameId: Long,
    val gameTitle: String,
    val platform: String,
    val coverUrl: String,
    val startTimestamp: Long = System.currentTimeMillis(),
    val accumulatedElapsedMs: Long = 0L,
    val lastResumeRealtimeMs: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val isPaused: Boolean = false
) {
    val formattedTime: String
        get() {
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
}

object SessionManager {
    private const val PREFS_NAME = "playpulse_session_persistence"
    private const val KEY_HAS_ACTIVE = "has_active_session"
    private const val KEY_GAME_ID = "game_id"
    private const val KEY_GAME_TITLE = "game_title"
    private const val KEY_PLATFORM = "platform"
    private const val KEY_COVER_URL = "cover_url"
    private const val KEY_START_TIMESTAMP = "start_timestamp"
    private const val KEY_ACCUMULATED_MS = "accumulated_elapsed_ms"
    private const val KEY_LAST_RESUME_WALL_MS = "last_resume_wall_ms"
    private const val KEY_IS_PAUSED = "is_paused"

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var isAppInForeground: Boolean = false

    private val _activeSession = MutableStateFlow<ActiveGameSession?>(null)
    val activeSession: StateFlow<ActiveGameSession?> = _activeSession.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun saveSessionToPrefs(
        context: Context,
        session: ActiveGameSession,
        lastResumeWallMs: Long = System.currentTimeMillis()
    ) {
        try {
            getPrefs(context).edit()
                .putBoolean(KEY_HAS_ACTIVE, true)
                .putLong(KEY_GAME_ID, session.gameId)
                .putString(KEY_GAME_TITLE, session.gameTitle)
                .putString(KEY_PLATFORM, session.platform)
                .putString(KEY_COVER_URL, session.coverUrl)
                .putLong(KEY_START_TIMESTAMP, session.startTimestamp)
                .putLong(KEY_ACCUMULATED_MS, session.accumulatedElapsedMs)
                .putLong(KEY_LAST_RESUME_WALL_MS, lastResumeWallMs)
                .putBoolean(KEY_IS_PAUSED, session.isPaused)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to save session to prefs", e)
        }
    }

    @Synchronized
    fun clearSessionFromPrefs(context: Context) {
        try {
            getPrefs(context).edit().clear().apply()
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to clear session from prefs", e)
        }
    }

    /**
     * Restores active session from persistent storage if present.
     * Accurately calculates all elapsed time passed during phone lock, sleep, or process death.
     */
    @Synchronized
    fun restoreSessionIfNeeded(context: Context): ActiveGameSession? {
        val inMemory = _activeSession.value
        if (inMemory != null) {
            return inMemory
        }

        val prefs = getPrefs(context)
        if (!prefs.getBoolean(KEY_HAS_ACTIVE, false)) {
            return null
        }

        val gameId = prefs.getLong(KEY_GAME_ID, -1L)
        if (gameId == -1L) {
            return null
        }

        val gameTitle = prefs.getString(KEY_GAME_TITLE, "") ?: ""
        val platform = prefs.getString(KEY_PLATFORM, "") ?: ""
        val coverUrl = prefs.getString(KEY_COVER_URL, "") ?: ""
        val startTimestamp = prefs.getLong(KEY_START_TIMESTAMP, System.currentTimeMillis())
        val savedAccumulatedMs = prefs.getLong(KEY_ACCUMULATED_MS, 0L)
        val savedLastResumeWallMs = prefs.getLong(KEY_LAST_RESUME_WALL_MS, System.currentTimeMillis())
        val isPaused = prefs.getBoolean(KEY_IS_PAUSED, false)

        val nowWall = System.currentTimeMillis()
        val nowRealtime = SystemClock.elapsedRealtime()

        // Calculate total time accurately: if not paused, add wall-clock difference since last resume
        val totalElapsedMs = if (isPaused) {
            savedAccumulatedMs
        } else {
            val deltaWall = (nowWall - savedLastResumeWallMs).coerceAtLeast(0L)
            savedAccumulatedMs + deltaWall
        }

        val currentSeconds = (totalElapsedMs / 1000L).coerceAtLeast(0L)

        val restoredSession = ActiveGameSession(
            gameId = gameId,
            gameTitle = gameTitle,
            platform = platform,
            coverUrl = coverUrl,
            startTimestamp = startTimestamp,
            accumulatedElapsedMs = totalElapsedMs,
            lastResumeRealtimeMs = nowRealtime,
            elapsedSeconds = currentSeconds,
            isPaused = isPaused
        )

        _activeSession.value = restoredSession

        // Save updated baseline immediately back to prefs
        saveSessionToPrefs(context, restoredSession, lastResumeWallMs = nowWall)

        if (isAppInForeground && !isPaused) {
            startTimerLoop()
        }

        return restoredSession
    }

    fun setAppInForeground(inForeground: Boolean, context: Context? = null) {
        isAppInForeground = inForeground
        val ctx = context ?: try { com.example.GamePalApplication.instance } catch (_: Exception) { null }

        if (inForeground) {
            val current = _activeSession.value ?: (ctx?.let { restoreSessionIfNeeded(it) })
            if (current != null && !current.isPaused) {
                val updatedSeconds = calculateElapsedSeconds(
                    accumulatedMs = current.accumulatedElapsedMs,
                    lastResumeMs = current.lastResumeRealtimeMs,
                    isPaused = false
                )
                _activeSession.value = current.copy(elapsedSeconds = updatedSeconds)
                startTimerLoop()
            }
        } else {
            // App entered background or phone locked:
            // Save current snapshot to disk so if OS kills process, no seconds are lost
            val current = _activeSession.value
            if (current != null && ctx != null) {
                if (!current.isPaused) {
                    val nowRealtime = SystemClock.elapsedRealtime()
                    val totalMs = current.accumulatedElapsedMs + (nowRealtime - current.lastResumeRealtimeMs)
                    val nowWall = System.currentTimeMillis()
                    saveSessionToPrefs(ctx, current.copy(accumulatedElapsedMs = totalMs), lastResumeWallMs = nowWall)
                } else {
                    saveSessionToPrefs(ctx, current, lastResumeWallMs = System.currentTimeMillis())
                }
            }
            // Stop coroutine ticker in background to consume 0% battery;
            // Hardware chronometer handles lockscreen notification without CPU wakeups
            timerJob?.cancel()
            timerJob = null
        }
    }

    private fun calculateElapsedSeconds(
        accumulatedMs: Long,
        lastResumeMs: Long,
        isPaused: Boolean
    ): Long {
        val totalMs = if (isPaused) {
            accumulatedMs
        } else {
            val nowRealtime = SystemClock.elapsedRealtime()
            accumulatedMs + (nowRealtime - lastResumeMs)
        }
        return (totalMs / 1000L).coerceAtLeast(0L)
    }

    fun startSession(context: Context, game: GameEntity) {
        val current = _activeSession.value
        if (current != null && current.gameId == game.id) {
            if (current.isPaused) {
                resumeSession(context)
            }
            return
        }

        val nowRealtime = SystemClock.elapsedRealtime()
        val nowWall = System.currentTimeMillis()
        val session = ActiveGameSession(
            gameId = game.id,
            gameTitle = game.title,
            platform = game.platform,
            coverUrl = game.coverUrl,
            startTimestamp = nowWall,
            accumulatedElapsedMs = 0L,
            lastResumeRealtimeMs = nowRealtime,
            elapsedSeconds = 0L,
            isPaused = false
        )
        _activeSession.value = session
        saveSessionToPrefs(context, session, lastResumeWallMs = nowWall)
        startTimerLoop()
        startForegroundService(context)
    }

    fun pauseSession(context: Context) {
        val current = _activeSession.value ?: return
        if (!current.isPaused) {
            val nowRealtime = SystemClock.elapsedRealtime()
            val totalMs = current.accumulatedElapsedMs + (nowRealtime - current.lastResumeRealtimeMs)
            val currentSeconds = (totalMs / 1000L).coerceAtLeast(0L)
            val nowWall = System.currentTimeMillis()

            val updated = current.copy(
                isPaused = true,
                accumulatedElapsedMs = totalMs,
                lastResumeRealtimeMs = nowRealtime,
                elapsedSeconds = currentSeconds
            )
            _activeSession.value = updated
            saveSessionToPrefs(context, updated, lastResumeWallMs = nowWall)

            timerJob?.cancel()
            timerJob = null
            updateForegroundService(context)
        }
    }

    fun resumeSession(context: Context) {
        val current = _activeSession.value ?: return
        if (current.isPaused) {
            val nowRealtime = SystemClock.elapsedRealtime()
            val nowWall = System.currentTimeMillis()
            val currentSeconds = (current.accumulatedElapsedMs / 1000L).coerceAtLeast(0L)

            val updated = current.copy(
                isPaused = false,
                lastResumeRealtimeMs = nowRealtime,
                elapsedSeconds = currentSeconds
            )
            _activeSession.value = updated
            saveSessionToPrefs(context, updated, lastResumeWallMs = nowWall)

            startTimerLoop()
            updateForegroundService(context)
        }
    }

    fun addMinutes(minutes: Int, context: Context? = null) {
        val current = _activeSession.value ?: return
        val addedMs = minutes * 60 * 1000L
        val newAccumulated = (current.accumulatedElapsedMs + addedMs).coerceAtLeast(0L)
        val newSeconds = calculateElapsedSeconds(
            accumulatedMs = newAccumulated,
            lastResumeMs = current.lastResumeRealtimeMs,
            isPaused = current.isPaused
        )
        val updated = current.copy(
            accumulatedElapsedMs = newAccumulated,
            elapsedSeconds = newSeconds
        )
        _activeSession.value = updated

        val ctx = context ?: try { com.example.GamePalApplication.instance } catch (_: Exception) { null }
        if (ctx != null) {
            saveSessionToPrefs(ctx, updated, lastResumeWallMs = System.currentTimeMillis())
            updateForegroundService(ctx)
        }
    }

    fun stopSession(context: Context): ActiveGameSession? {
        val current = _activeSession.value ?: restoreSessionIfNeeded(context)
        timerJob?.cancel()
        timerJob = null
        _activeSession.value = null
        clearSessionFromPrefs(context)
        stopForegroundService(context)

        if (current == null) return null
        val finalSeconds = calculateElapsedSeconds(
            accumulatedMs = current.accumulatedElapsedMs,
            lastResumeMs = current.lastResumeRealtimeMs,
            isPaused = current.isPaused
        )
        return current.copy(elapsedSeconds = finalSeconds)
    }

    fun cancelSession(context: Context) {
        timerJob?.cancel()
        timerJob = null
        _activeSession.value = null
        clearSessionFromPrefs(context)
        stopForegroundService(context)
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        if (!isAppInForeground) return

        timerJob = scope.launch {
            while (isActive && isAppInForeground) {
                delay(1000L)
                val current = _activeSession.value
                if (current != null && !current.isPaused) {
                    val updatedSeconds = calculateElapsedSeconds(
                        accumulatedMs = current.accumulatedElapsedMs,
                        lastResumeMs = current.lastResumeRealtimeMs,
                        isPaused = false
                    )
                    _activeSession.value = current.copy(elapsedSeconds = updatedSeconds)
                } else {
                    break
                }
            }
        }
    }

    fun ensureForegroundServiceRunning(context: Context) {
        val current = _activeSession.value ?: return
        try {
            val intent = Intent(context.applicationContext, GameSessionTrackingService::class.java).apply {
                action = GameSessionTrackingService.ACTION_UPDATE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.applicationContext.startForegroundService(intent)
            } else {
                context.applicationContext.startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Error ensuring foreground service", e)
        }
    }

    private fun startForegroundService(context: Context) {
        try {
            val intent = Intent(context.applicationContext, GameSessionTrackingService::class.java).apply {
                action = GameSessionTrackingService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.applicationContext.startForegroundService(intent)
            } else {
                context.applicationContext.startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Error starting foreground service", e)
        }
    }

    private fun updateForegroundService(context: Context) {
        try {
            val intent = Intent(context.applicationContext, GameSessionTrackingService::class.java).apply {
                action = GameSessionTrackingService.ACTION_UPDATE
            }
            context.applicationContext.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Error updating foreground service", e)
        }
    }

    private fun stopForegroundService(context: Context) {
        try {
            val intent = Intent(context.applicationContext, GameSessionTrackingService::class.java).apply {
                action = GameSessionTrackingService.ACTION_STOP
            }
            context.applicationContext.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Error stopping foreground service", e)
        }
    }
}

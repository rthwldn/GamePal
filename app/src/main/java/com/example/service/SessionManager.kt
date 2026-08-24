package com.example.service

import android.content.Context
import android.content.Intent
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
    val lastResumeRealtimeMs: Long = SystemClock.elapsedRealtime(),
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
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _activeSession = MutableStateFlow<ActiveGameSession?>(null)
    val activeSession: StateFlow<ActiveGameSession?> = _activeSession.asStateFlow()

    /**
     * Calculates the accurate elapsed seconds using SystemClock.elapsedRealtime(),
     * which continues ticking accurately even when the device is locked or in deep sleep.
     */
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
            // Already tracking this game
            if (current.isPaused) {
                resumeSession(context)
            }
            return
        }

        val nowRealtime = SystemClock.elapsedRealtime()
        val session = ActiveGameSession(
            gameId = game.id,
            gameTitle = game.title,
            platform = game.platform,
            coverUrl = game.coverUrl,
            startTimestamp = System.currentTimeMillis(),
            accumulatedElapsedMs = 0L,
            lastResumeRealtimeMs = nowRealtime,
            elapsedSeconds = 0L,
            isPaused = false
        )
        _activeSession.value = session
        startTimerLoop()
        startForegroundService(context)
    }

    fun pauseSession(context: Context) {
        val current = _activeSession.value ?: return
        if (!current.isPaused) {
            val nowRealtime = SystemClock.elapsedRealtime()
            val totalMs = current.accumulatedElapsedMs + (nowRealtime - current.lastResumeRealtimeMs)
            val currentSeconds = (totalMs / 1000L).coerceAtLeast(0L)

            _activeSession.value = current.copy(
                isPaused = true,
                accumulatedElapsedMs = totalMs,
                lastResumeRealtimeMs = nowRealtime,
                elapsedSeconds = currentSeconds
            )
            timerJob?.cancel()
            updateForegroundService(context)
        }
    }

    fun resumeSession(context: Context) {
        val current = _activeSession.value ?: return
        if (current.isPaused) {
            val nowRealtime = SystemClock.elapsedRealtime()
            val currentSeconds = (current.accumulatedElapsedMs / 1000L).coerceAtLeast(0L)

            _activeSession.value = current.copy(
                isPaused = false,
                lastResumeRealtimeMs = nowRealtime,
                elapsedSeconds = currentSeconds
            )
            startTimerLoop()
            updateForegroundService(context)
        }
    }

    fun addMinutes(minutes: Int) {
        val current = _activeSession.value ?: return
        val addedMs = minutes * 60 * 1000L
        val newAccumulated = (current.accumulatedElapsedMs + addedMs).coerceAtLeast(0L)
        val newSeconds = calculateElapsedSeconds(
            accumulatedMs = newAccumulated,
            lastResumeMs = current.lastResumeRealtimeMs,
            isPaused = current.isPaused
        )
        _activeSession.value = current.copy(
            accumulatedElapsedMs = newAccumulated,
            elapsedSeconds = newSeconds
        )
    }

    fun stopSession(context: Context): ActiveGameSession? {
        val current = _activeSession.value
        timerJob?.cancel()
        _activeSession.value = null
        stopForegroundService(context)

        if (current == null) return null
        val finalSeconds = calculateElapsedSeconds(
            accumulatedMs = current.accumulatedElapsedMs,
            lastResumeMs = current.lastResumeRealtimeMs,
            isPaused = current.isPaused
        )
        return current.copy(elapsedSeconds = finalSeconds)
    }

    /**
     * Periodic loop to refresh UI states.
     * Crucially, instead of incrementing +1 on every tick (which loses time during CPU sleep/lock),
     * it evaluates the actual elapsed real time since lastResumeRealtimeMs.
     */
    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val current = _activeSession.value
                if (current != null && !current.isPaused) {
                    val updatedSeconds = calculateElapsedSeconds(
                        accumulatedMs = current.accumulatedElapsedMs,
                        lastResumeMs = current.lastResumeRealtimeMs,
                        isPaused = false
                    )
                    _activeSession.value = current.copy(elapsedSeconds = updatedSeconds)
                }
            }
        }
    }

    fun cancelSession(context: Context) {
        timerJob?.cancel()
        _activeSession.value = null
        stopForegroundService(context)
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

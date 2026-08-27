package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameSessionTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var notificationManager: NotificationManager
    private var lastWasPaused: Boolean? = null
    private var lastGameId: Long? = null

    companion object {
        const val CHANNEL_ID = "gamepal_live_activity_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_UPDATE = "com.example.service.ACTION_UPDATE"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_TOGGLE_PAUSE = "com.example.service.ACTION_TOGGLE_PAUSE"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Live Activity - Herní čas",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Zobrazuje aktivní čas hraní na zamčené obrazovce a v liště oznámení"
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(this)

        serviceScope.launch {
            SessionManager.activeSession.collectLatest { session ->
                if (session == null) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    // Update notification if status changed or session started/switched
                    val shouldUpdateNotification = (session.isPaused != lastWasPaused) || (session.gameId != lastGameId)
                    if (shouldUpdateNotification) {
                        lastWasPaused = session.isPaused
                        lastGameId = session.gameId
                        val notification = buildNotification(session)
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_TOGGLE_PAUSE -> {
                val current = SessionManager.activeSession.value
                if (current != null) {
                    if (current.isPaused) {
                        SessionManager.resumeSession(this)
                    } else {
                        SessionManager.pauseSession(this)
                    }
                }
            }
            ACTION_STOP -> {
                val finished = SessionManager.stopSession(this)
                if (finished != null && finished.elapsedSeconds > 0) {
                    serviceScope.launch(Dispatchers.IO) {
                        try {
                            val db = com.example.data.AppDatabase.getDatabase(applicationContext)
                            val repo = com.example.data.GameRepository(db.gameDao(), db.playSessionDao())
                            repo.recordSession(
                                gameId = finished.gameId,
                                durationSeconds = finished.elapsedSeconds,
                                startTime = finished.startTimestamp,
                                endTime = System.currentTimeMillis(),
                                sessionNotes = ""
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("GameSessionTrackingService", "Failed to save session on stop", e)
                        } finally {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                    }
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE, null -> {
                val current = SessionManager.activeSession.value
                if (current != null) {
                    lastWasPaused = current.isPaused
                    lastGameId = current.gameId
                    val notification = buildNotification(current)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            startForeground(
                                NOTIFICATION_ID,
                                notification,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                                } else {
                                    0
                                }
                            )
                        } else {
                            startForeground(NOTIFICATION_ID, notification)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GameSessionTrackingService", "startForeground failed", e)
                    }
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val current = SessionManager.activeSession.value
        if (current == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildNotification(session: ActiveGameSession): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ACTIVE_SESSION", true)
            putExtra("GAME_ID", session.gameId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle pause action
        val togglePauseIntent = Intent(this, GameSessionTrackingService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePausePendingIntent = PendingIntent.getService(
            this,
            1,
            togglePauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop session action
        val stopIntent = Intent(this, GameSessionTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseTitle = if (session.isPaused) "▶ Pokračovat" else "⏸ Pozastavit"
        val statusText = if (session.isPaused) {
            "Pozastaveno • ${session.formattedTime}"
        } else {
            "Aktivní hraní"
        }

        // Hardware chronometer base time: System clock minus total elapsed so far
        val baseWhen = System.currentTimeMillis() - (session.elapsedSeconds * 1000L)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🎮 ${session.gameTitle} (${session.platform})")
            .setContentText(statusText)
            .setSubText("Live Activity")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setUsesChronometer(!session.isPaused)
            .setWhen(baseWhen)
            .setShowWhen(true)
            .addAction(0, pauseTitle, togglePausePendingIntent)
            .addAction(0, "⏹ Ukončit", stopPendingIntent)

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

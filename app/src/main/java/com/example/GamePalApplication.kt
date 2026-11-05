package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.data.AppDatabase
import com.example.data.GameMetadataService
import com.example.data.GameRepository
import com.example.service.GameSessionTrackingService
import com.example.service.SessionManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GamePalApplication : Application(), ImageLoaderFactory {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: GameRepository
        private set

    lateinit var syncManager: com.example.data.firestore.FirestoreSyncManager
        private set

    val metadataService by lazy { GameMetadataService() }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        syncManager = com.example.data.firestore.FirestoreSyncManager(this, database.gameDao(), database.playSessionDao())
        repository = GameRepository(database.gameDao(), database.playSessionDao(), syncManager)

        // Initialize Live Activity Notification Channel
        GameSessionTrackingService.createNotificationChannel(this)

        // Restore active game session if process died or system restarted while session was active
        val restoredSession = SessionManager.restoreSessionIfNeeded(this)
        if (restoredSession != null && !restoredSession.isPaused) {
            SessionManager.ensureForegroundServiceRunning(this)
        }

        // Clear pre-populated sample games once so library is 100% clean for user
        val prefs = getSharedPreferences("gamepal_prefs", MODE_PRIVATE)
        val hasCleared = prefs.getBoolean("has_cleared_sample_data_v1", false)
        if (!hasCleared) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    database.clearAllTables()
                    prefs.edit().putBoolean("has_cleared_sample_data_v1", true).apply()
                    com.example.widget.GameTrackerWidgetProvider.updateAllWidgets(this@GamePalApplication)
                } catch (_: Exception) {}
            }
        }

        // Register Activity Lifecycle Callbacks to optimize battery:
        // Ensures timer loops strictly only tick when the UI is visible in foreground.
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var startedActivities = 0

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                startedActivities++
                if (startedActivities == 1) {
                    SessionManager.setAppInForeground(true, this@GamePalApplication)
                }
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                if (startedActivities == 0) {
                    SessionManager.setAppInForeground(false, this@GamePalApplication)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    companion object {
        lateinit var instance: GamePalApplication
            private set
    }
}

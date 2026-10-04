package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.data.AppDatabase
import com.example.data.GameMetadataService
import com.example.data.GameRepository
import com.example.service.GameSessionTrackingService
import com.example.service.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GamePalApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: GameRepository
        private set

    val metadataService by lazy { GameMetadataService() }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        repository = GameRepository(database.gameDao(), database.playSessionDao())

        // Initialize Live Activity Notification Channel
        GameSessionTrackingService.createNotificationChannel(this)

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
                    SessionManager.setAppInForeground(true)
                }
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                if (startedActivities == 0) {
                    SessionManager.setAppInForeground(false)
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

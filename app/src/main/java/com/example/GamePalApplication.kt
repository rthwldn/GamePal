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

        // Seed initial data if needed (runs once if DB empty)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.seedInitialDataIfNeeded()
            } catch (_: Exception) {}
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

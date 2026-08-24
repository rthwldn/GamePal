package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.GameMetadataService
import com.example.data.GameRepository
import com.example.service.GameSessionTrackingService
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

        // Seed initial data if needed
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.seedInitialDataIfNeeded()
            } catch (_: Exception) {}
        }
    }

    companion object {
        lateinit var instance: GamePalApplication
            private set
    }
}

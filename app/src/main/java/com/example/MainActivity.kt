package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.FrostedBottomBar
import com.example.ui.components.MainNavTab
import com.example.ui.screens.AddGameDialog
import com.example.ui.screens.GameDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GamePalViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GamePalViewModel by viewModels {
        val app = application as GamePalApplication
        GamePalViewModel.provideFactory(app.repository, app.metadataService)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val openGameId = intent.getLongExtra("GAME_ID", -1L)
        if (openGameId != -1L) {
            viewModel.selectGame(openGameId)
        }
        com.example.widget.GameTrackerWidgetProvider.updateAllWidgets(this)
    }

    override fun onResume() {
        super.onResume()
        com.example.widget.GameTrackerWidgetProvider.updateAllWidgets(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // Check if opened from Live Activity notification
        val openGameId = intent.getLongExtra("GAME_ID", -1L)
        if (openGameId != -1L) {
            viewModel.selectGame(openGameId)
        }

        setContent {
            // Request Notification Permission on Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    GamePalApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GamePalApp(viewModel: GamePalViewModel) {
    val selectedGame by viewModel.selectedGame.collectAsState()
    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Gesture navigation handlers
    BackHandler(enabled = selectedGame != null) {
        viewModel.selectGame(null)
    }

    BackHandler(enabled = selectedGame == null && showAddDialog) {
        showAddDialog = false
    }

    BackHandler(enabled = selectedGame == null && !showAddDialog && currentTab != MainNavTab.HOME) {
        currentTab = MainNavTab.HOME
    }

    val blurRadius by animateDpAsState(
        targetValue = if (showAddDialog) 24.dp else 0.dp,
        label = "DialogBackdropBlur"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedGame,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurRadius)
        ) { game ->
            if (game != null) {
                GameDetailScreen(
                    game = game,
                    viewModel = viewModel,
                    onBack = { viewModel.selectGame(null) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabTransition",
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        when (tab) {
                            MainNavTab.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onGameClick = { gameId -> viewModel.selectGame(gameId) },
                                    onOpenAddGame = { showAddDialog = true }
                                )
                            }
                            MainNavTab.STATS -> {
                                StatsScreen(
                                    viewModel = viewModel,
                                    onGameClick = { gameId -> viewModel.selectGame(gameId) },
                                    onOpenAddGame = { showAddDialog = true }
                                )
                            }
                        }
                    }

                    // Frosted Glass Bottom Navigation Bar
                    FrostedBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onAddClick = { showAddDialog = true },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        if (showAddDialog) {
            AddGameDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

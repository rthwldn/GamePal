package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.model.PlatformInfo
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.firestore.SyncState
import com.example.ui.components.ChangeCoverDialog
import com.example.ui.components.GoogleBackupDialog
import com.example.ui.components.GameCoverImage
import com.example.ui.components.LiveActivityBanner
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.GamePalPrimary
import com.example.ui.theme.GamePalSecondary
import com.example.ui.theme.RatingGold
import com.example.ui.theme.auroraBackground
import com.example.ui.theme.glassCard
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.viewmodel.GamePalViewModel
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: GamePalViewModel,
    syncManager: FirestoreSyncManager,
    onGameClick: (Long) -> Unit,
    onOpenAddGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val games by viewModel.filteredGames.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedPlatform by viewModel.selectedPlatformFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.sortOption.collectAsState()
    val syncState by syncManager.syncState.collectAsState()

    val activeGameId by remember { derivedStateOf { activeSession?.gameId } }
    val popularPlatforms = remember {
        listOf("PC", "PS5", "Xbox Series", "Switch", "Switch 2", "Ostatní")
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var gameToChangeCover by remember { mutableStateOf<GameEntity?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }

    // Gesture navigation handler: if searching or filtering, back clears search or filters
    val hasActiveFilterOrSearch by remember {
        derivedStateOf { searchQuery.isNotEmpty() || selectedStatus != null || selectedPlatform != null }
    }
    BackHandler(enabled = hasActiveFilterOrSearch) {
        if (searchQuery.isNotEmpty()) {
            viewModel.searchQuery.value = ""
        } else {
            viewModel.selectedStatusFilter.value = null
            viewModel.selectedPlatformFilter.value = null
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier.auroraBackground(isDark = true)
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 0. App Title & Google Backup Status Pill
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE2E8F0)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFF09090B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PlayPulse",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Google Cloud Backup Indicator Pill (M3 Expressive Pill)
                    val backupShape = RoundedCornerShape(percent = 50)
                    Box(
                        modifier = Modifier
                            .clip(backupShape)
                            .background(Color(0xFF13131A))
                            .border(1.dp, Color(0xFF262634), backupShape)
                            .clickable { showBackupDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("google_backup_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val user = Firebase.auth.currentUser
                            if (user == null) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFFA1A1AA),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Záloha: Vypnuto",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFA1A1AA)
                                )
                            } else {
                                when (syncState) {
                                    is SyncState.Syncing -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = GamePalPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Zálohování...",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = GamePalPrimary
                                        )
                                    }
                                    is SyncState.Error -> {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Chyba zálohy",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Google záloha",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. Live Activity Ongoing Session Banner (Top Priority)
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveActivityBanner(
                    activeSession = activeSession,
                    onBannerClick = { gameId -> onGameClick(gameId) },
                    onTogglePause = {
                        if (activeSession?.isPaused == true) {
                            viewModel.resumeSession(context)
                        } else {
                            viewModel.pauseSession(context)
                        }
                    },
                    onStopSession = { viewModel.stopSession(context) }
                )
            }

            // 2. Library Statistics Header Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(shape = RoundedCornerShape(20.dp), isDark = true)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            title = "Celkem her",
                            value = "${stats.totalGames}",
                            accent = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color(0xFF27272A))
                        )
                        StatItem(
                            title = "Odehráno",
                            value = stats.totalHours,
                            accent = Color(0xFFD4D4D8)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color(0xFF27272A))
                        )
                        StatItem(
                            title = "Dohráno",
                            value = "${stats.completedGames}",
                            accent = Color(0xFFA1A1AA)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color(0xFF27272A))
                        )
                        StatItem(
                            title = "Průměr",
                            value = if (stats.averageRating > 0) String.format("%.1f★", stats.averageRating) else "—",
                            accent = RatingGold
                        )
                    }
                }
            }

            // 3. Search Field (M3 Expressive Pill)
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_games_input"),
                    placeholder = { Text("Hledat v knihovně...", color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Hledat",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazat", tint = Color.White)
                            }
                        }
                    },
                    shape = RoundedCornerShape(percent = 50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF262634),
                        focusedContainerColor = Color(0xFF13131A),
                        unfocusedContainerColor = Color(0xFF0F0F14),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // 4. Status Filter Chips & Sort Controls
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            val isAll = selectedStatus == null || selectedStatus == "ALL"
                            FilterChipPill(
                                label = "Vše (${stats.totalGames})",
                                isSelected = isAll,
                                activeColor = GamePalPrimary,
                                onClick = { viewModel.selectedStatusFilter.value = null }
                            )
                        }

                        items(GameStatus.entries) { status ->
                            val isSel = selectedStatus == status.id
                            FilterChipPill(
                                label = status.titleCs,
                                isSelected = isSel,
                                activeColor = status.primaryColor,
                                onClick = {
                                    viewModel.selectedStatusFilter.value = if (isSel) null else status.id
                                }
                            )
                        }
                    }

                    // Platform & Sort bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Platform chips (popular quick filters)
                        val chipShape = RoundedCornerShape(percent = 50)
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                val isAll = selectedPlatform == null
                                Box(
                                    modifier = Modifier
                                        .clip(chipShape)
                                        .background(
                                            if (isAll) Color.White else Color(0xFF13131A)
                                        )
                                        .border(
                                            1.dp,
                                            if (isAll) Color.White else Color(0xFF262634),
                                            chipShape
                                        )
                                        .clickable { viewModel.selectedPlatformFilter.value = null }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Vše",
                                        fontSize = 11.sp,
                                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isAll) Color.Black else Color(0xFF94A3B8)
                                    )
                                }
                            }

                            popularPlatforms.forEach { pShort ->
                                val p = PlatformInfo.findByName(pShort)
                                val isSel = selectedPlatform.equals(p.name, ignoreCase = true)
                                item(key = "platform_$pShort") {
                                    Box(
                                        modifier = Modifier
                                            .clip(chipShape)
                                            .background(
                                                if (isSel) p.brandColor.copy(alpha = 0.35f)
                                                else Color(0xFF13131A)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSel) p.brandColor else Color(0xFF262634),
                                                chipShape
                                            )
                                            .clickable {
                                                viewModel.selectedPlatformFilter.value = if (isSel) null else p.name
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = p.shortName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) p.accentColor else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Sort Menu Dropdown (Expressive Circle)
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF13131A))
                                    .border(1.dp, Color(0xFF262634), CircleShape)
                                    .clickable { showSortMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Řazení",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.label,
                                                fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                                color = if (currentSort == option) GamePalPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.sortOption.value = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Game Cards List / Grid
            if (games.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                            .glassCard(shape = RoundedCornerShape(24.dp), isDark = true)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = GamePalPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Žádné hry nenalezeny",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedStatus != null)
                                    "Zkuste upravit vyhledávání nebo filtry"
                                else
                                    "Přidejte svou první hru tlačítkem + ve spodní liště!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenAddGame,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Přidat hru", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(games, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        isTracking = activeGameId == game.id,
                        onClick = { onGameClick(game.id) },
                        onQuickPlay = { viewModel.startSession(context, game) },
                        onChangeCover = { gameToChangeCover = game },
                        isDark = true
                    )
                }
            }
        }
    }

    // Change cover dialog from long-press or edit button on Home grid
    gameToChangeCover?.let { targetGame ->
        ChangeCoverDialog(
            game = targetGame,
            onCoverSelected = { newCover ->
                viewModel.updateGameCover(targetGame.id, newCover)
                gameToChangeCover = null
            },
            onSearchOnline = { query ->
                viewModel.searchOnlineCovers(query)
            },
            onDismiss = { gameToChangeCover = null }
        )
    }

    if (showBackupDialog) {
        GoogleBackupDialog(
            syncManager = syncManager,
            onDismiss = { showBackupDialog = false },
            onSignOutComplete = { showBackupDialog = false }
        )
    }
}

@Composable
fun StatItem(title: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = accent
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val chipShape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(
                if (isSelected) Color.White else Color(0xFF13131A)
            )
            .border(
                1.dp,
                if (isSelected) Color.White else Color(0xFF262634),
                chipShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun GameCard(
    game: GameEntity,
    isTracking: Boolean,
    onClick: () -> Unit,
    onQuickPlay: () -> Unit,
    onChangeCover: () -> Unit = {},
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val status = GameStatus.fromString(game.status)
    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(
                shape = cardShape,
                isDark = isDark,
                accentBorder = isTracking,
                customTint = if (isTracking) Color(0xFF222230) else null
            )
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Column {
            // Poster Cover Image with Badge Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            ) {
                GameCoverImage(
                    coverUrl = game.coverUrl,
                    contentDescription = game.title,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 8
                )

                // Top Left: Rating Score Star Badge (M3 Expressive Pill)
                if (game.rating != null && game.rating > 0) {
                    val starPill = RoundedCornerShape(percent = 50)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(starPill)
                            .background(Color(0xE6000000))
                            .border(0.5.dp, RatingGold.copy(alpha = 0.5f), starPill)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = RatingGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${game.rating}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Top Right: Status dot / pill (M3 Expressive Pill)
                val statusPill = RoundedCornerShape(percent = 50)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(statusPill)
                        .background(status.primaryColor.copy(alpha = 0.95f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status.titleCs,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // If no cover, display a clear "Změnit obal" badge
                if (game.coverUrl.isBlank()) {
                    val editPill = RoundedCornerShape(percent = 50)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(editPill)
                            .background(Color(0xE6000000))
                            .border(1.dp, Color(0x60FFFFFF), editPill)
                            .clickable(onClick = onChangeCover)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Nastavit obal",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+ Obal",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Bottom Left: Change Cover icon (Circle)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xD9000000))
                        .border(0.5.dp, Color(0x40FFFFFF), CircleShape)
                        .clickable(onClick = onChangeCover),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Změnit obal hry",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(11.dp)
                    )
                }

                // Bottom Right: Quick Play / Live Activity Button (Circle)
                if (!isTracking) {
                    IconButton(
                        onClick = onQuickPlay,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Spustit měření",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Game Title
            Text(
                text = game.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Platform Badge + Total Play Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformBadge(
                    platformName = game.platform,
                    compact = true
                )

                Text(
                    text = game.formattedTotalTime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTracking) GamePalSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

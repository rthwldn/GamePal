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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.GameCoverImage
import com.example.ui.components.LiveActivityBanner
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.GamePalPrimary
import com.example.ui.theme.GamePalSecondary
import com.example.ui.theme.RatingGold
import com.example.ui.theme.auroraBackground
import com.example.ui.theme.glassCard
import com.example.viewmodel.GamePalViewModel
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: GamePalViewModel,
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

    var showSortMenu by remember { mutableStateOf(false) }

    // Gesture navigation handler: if searching or filtering, back clears search or filters
    val hasActiveFilterOrSearch = searchQuery.isNotEmpty() || selectedStatus != null || selectedPlatform != null
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
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                        .glassCard(shape = RoundedCornerShape(12.dp), isDark = true)
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

            // 3. Search Field
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_games_input"),
                    placeholder = { Text("Hledat v knihovně...") },
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
                                Icon(Icons.Default.Clear, contentDescription = "Vymazat")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF27272A),
                        focusedContainerColor = Color(0xFF141417),
                        unfocusedContainerColor = Color(0xFF101013)
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
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                val isAll = selectedPlatform == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isAll) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable { viewModel.selectedPlatformFilter.value = null }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Všechny platformy",
                                        fontSize = 11.sp,
                                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isAll) GamePalPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            listOf("PS5", "Xbox Series", "Switch", "Deck", "PC", "PS4", "PS2", "SNES", "NES").forEach { pShort ->
                                val p = PlatformInfo.findByName(pShort)
                                val isSel = selectedPlatform.equals(p.name, ignoreCase = true)
                                item {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSel) p.brandColor.copy(alpha = 0.3f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            )
                                            .clickable {
                                                viewModel.selectedPlatformFilter.value = if (isSel) null else p.name
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = p.shortName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) p.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Sort Menu Dropdown
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Řazení",
                                    tint = GamePalPrimary,
                                    modifier = Modifier.size(20.dp)
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
                        isTracking = activeSession?.gameId == game.id,
                        onClick = { onGameClick(game.id) },
                        onQuickPlay = { viewModel.startSession(context, game) },
                        isDark = true
                    )
                }
            }
        }
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) Color.White else Color(0xFF18181B)
            )
            .border(
                1.dp,
                if (isSelected) Color.White else Color(0xFF27272A),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color(0xFFA1A1AA)
        )
    }
}

@Composable
fun GameCard(
    game: GameEntity,
    isTracking: Boolean,
    onClick: () -> Unit,
    onQuickPlay: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val status = GameStatus.fromString(game.status)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(
                shape = RoundedCornerShape(12.dp),
                isDark = isDark,
                accentBorder = isTracking,
                customTint = if (isTracking) Color(0xFF27272A) else null
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            // Poster Cover Image with Badge Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                GameCoverImage(
                    coverUrl = game.coverUrl,
                    contentDescription = game.title,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 8
                )

                // Top Left: Rating Score Star Badge
                if (game.rating != null && game.rating > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xE609090B))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = RatingGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${game.rating}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Top Right: Status dot / pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(status.primaryColor.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status.titleCs,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottom Right: Quick Play / Live Activity Button
                if (!isTracking) {
                    IconButton(
                        onClick = onQuickPlay,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Spustit měření",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Title
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTracking) GamePalSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

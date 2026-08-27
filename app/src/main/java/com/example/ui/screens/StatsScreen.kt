package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.GamePalEmerald
import com.example.ui.theme.GamePalPrimary
import com.example.ui.theme.GamePalSecondary
import com.example.ui.theme.RatingGold
import com.example.ui.theme.auroraBackground
import com.example.ui.theme.glassCard
import com.example.viewmodel.GamePalViewModel
import java.util.Locale
import kotlin.math.roundToInt

// Data model for aggregated Genre Stats
data class GenreAnalyticsItem(
    val name: String,
    val count: Int,
    val totalSeconds: Long,
    val percentage: Float,
    val color: Color
)

// Data model for aggregated Platform Stats
data class PlatformAnalyticsItem(
    val name: String,
    val count: Int,
    val totalSeconds: Long,
    val percentage: Float,
    val platformInfo: PlatformInfo
)

// Color palette for charts
val GenrePalette = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFF06B6D4), // Cyan
    Color(0xFFEC4899), // Pink
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Violet
    Color(0xFF3B82F6), // Blue
    Color(0xFFF97316), // Orange
    Color(0xFF14B8A6), // Teal
    Color(0xFFE11D48)  // Rose
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: GamePalViewModel,
    onGameClick: (Long) -> Unit,
    onOpenAddGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allGames by viewModel.allGames.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    // Chart animation progress
    val chartAnimation = remember { Animatable(0f) }
    LaunchedEffect(allGames.size) {
        chartAnimation.snapTo(0f)
        chartAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    // Aggregations
    val totalGames = allGames.size
    val totalPlaySeconds = allGames.sumOf { it.totalPlayTimeSeconds }
    val totalHours = totalPlaySeconds / 3600
    val totalMinutes = (totalPlaySeconds % 3600) / 60
    val completedGamesCount = allGames.count { it.status == GameStatus.COMPLETED.id }
    val playingGamesCount = allGames.count { it.status == GameStatus.PLAYING.id }
    val backlogGamesCount = allGames.count { it.status == GameStatus.BACKLOG.id }
    val pausedGamesCount = allGames.count { it.status == GameStatus.PAUSED.id }
    val droppedGamesCount = allGames.count { it.status == GameStatus.DROPPED.id }

    val completionRate = if (totalGames > 0) (completedGamesCount.toFloat() / totalGames * 100f) else 0f

    val ratedGames = allGames.filter { it.rating != null && it.rating > 0 }
    val avgRating = if (ratedGames.isNotEmpty()) ratedGames.map { it.rating!! }.average() else 0.0

    // Top rated games (Hall of Fame)
    val hallOfFame = remember(allGames) {
        allGames.filter { (it.rating ?: 0) >= 8 }
            .sortedWith(compareByDescending<GameEntity> { it.rating }.thenByDescending { it.totalPlayTimeSeconds })
    }

    // Most played game
    val mostPlayedGame = remember(allGames) {
        allGames.maxByOrNull { it.totalPlayTimeSeconds }
    }

    // Genre Analytics Aggregation
    val genreStats = remember(allGames) {
        val map = mutableMapOf<String, Pair<Int, Long>>() // Genre -> (count, totalSec)
        allGames.forEach { game ->
            val genresList = if (game.genres.isNotBlank()) {
                game.genres.split(",", "/", ";", "•", "|").map { it.trim() }.filter { it.isNotBlank() }
            } else listOf("Ostatní")

            genresList.forEach { g ->
                val curr = map.getOrDefault(g, Pair(0, 0L))
                map[g] = Pair(curr.first + 1, curr.second + game.totalPlayTimeSeconds)
            }
        }

        val totalGenreHits = map.values.sumOf { it.first }.coerceAtLeast(1)
        map.entries
            .sortedByDescending { it.value.first }
            .take(8)
            .mapIndexed { index, entry ->
                GenreAnalyticsItem(
                    name = entry.key,
                    count = entry.value.first,
                    totalSeconds = entry.value.second,
                    percentage = (entry.value.first.toFloat() / totalGenreHits) * 100f,
                    color = GenrePalette[index % GenrePalette.size]
                )
            }
    }

    // Platform Analytics Aggregation
    val platformStats = remember(allGames) {
        val grouped = allGames.groupBy { it.platform }
        grouped.entries
            .sortedByDescending { it.value.size }
            .map { entry ->
                val pInfo = PlatformInfo.findByName(entry.key)
                val sec = entry.value.sumOf { it.totalPlayTimeSeconds }
                PlatformAnalyticsItem(
                    name = entry.key,
                    count = entry.value.size,
                    totalSeconds = sec,
                    percentage = (entry.value.size.toFloat() / totalGames.coerceAtLeast(1)) * 100f,
                    platformInfo = pInfo
                )
            }
    }

    // Rating Distribution (1 to 10)
    val ratingDistribution = remember(allGames) {
        (1..10).map { score ->
            val count = allGames.count { it.rating == score }
            Pair(score, count)
        }
    }

    // Sessions metrics
    val totalSessionsCount = allSessions.size
    val longestSessionSec = allSessions.maxOfOrNull { it.durationSeconds } ?: 0L
    val avgSessionSec = if (totalSessionsCount > 0) allSessions.sumOf { it.durationSeconds } / totalSessionsCount else 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GamePalPrimary, GamePalSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Statistiky",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Přehled herního času a knihovny",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.auroraBackground(isDark = true)
    ) { innerPadding ->
        if (totalGames == 0) {
            // Empty State
            EmptyStatsView(
                isDark = true,
                onAddGameClick = onOpenAddGame,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Highlights 2x2 Grid
                item {
                    HeroHighlightsGrid(
                        totalHours = totalHours,
                        totalMinutes = totalMinutes,
                        totalGames = totalGames,
                        completedCount = completedGamesCount,
                        completionRate = completionRate,
                        avgRating = avgRating,
                        totalSessions = totalSessionsCount,
                        isDark = true
                    )
                }

                // 2. Favorite Game / Most Played Spotlight
                if (mostPlayedGame != null && mostPlayedGame.totalPlayTimeSeconds > 0) {
                    item {
                        MostPlayedSpotlightCard(
                            game = mostPlayedGame,
                            onClick = { onGameClick(mostPlayedGame.id) },
                            isDark = true
                        )
                    }
                }

                // 3. Typy her & Žánry (Genre Breakdown Chart)
                if (genreStats.isNotEmpty()) {
                    item {
                        GenreBreakdownCard(
                            genreStats = genreStats,
                            animationProgress = chartAnimation.value,
                            isDark = true
                        )
                    }
                }

                // 4. Herní platformy (Platform Breakdown Chart)
                if (platformStats.isNotEmpty()) {
                    item {
                        PlatformBreakdownCard(
                            platformStats = platformStats,
                            animationProgress = chartAnimation.value,
                            isDark = true
                        )
                    }
                }

                // 5. Hodnocení her (Rating Distribution 1–10⭐)
                item {
                    RatingDistributionCard(
                        ratingDistribution = ratingDistribution,
                        ratedCount = ratedGames.size,
                        avgRating = avgRating,
                        animationProgress = chartAnimation.value,
                        isDark = true
                    )
                }

                // 6. Hall of Fame (Nejlépe hodnocené hry 8–10)
                if (hallOfFame.isNotEmpty()) {
                    item {
                        HallOfFameSection(
                            games = hallOfFame,
                            onGameClick = onGameClick,
                            isDark = true
                        )
                    }
                }

                // 7. Stavy v knihovně & Herní aktivita
                item {
                    StatusBreakdownCard(
                        playingCount = playingGamesCount,
                        completedCount = completedGamesCount,
                        backlogCount = backlogGamesCount,
                        pausedCount = pausedGamesCount,
                        droppedCount = droppedGamesCount,
                        totalGames = totalGames,
                        totalSessions = totalSessionsCount,
                        avgSessionSec = avgSessionSec,
                        longestSessionSec = longestSessionSec,
                        isDark = true
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// Subcomponents & Visual Cards
// ---------------------------------------------------------------------------------

@Composable
private fun HeroHighlightsGrid(
    totalHours: Long,
    totalMinutes: Long,
    totalGames: Int,
    completedCount: Int,
    completionRate: Float,
    avgRating: Double,
    totalSessions: Int,
    isDark: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Playtime
            HeroStatCard(
                title = "Celkový herní čas",
                value = if (totalHours > 0) "${totalHours}h ${totalMinutes}m" else "${totalMinutes}m",
                subtitle = "$totalSessions herních relací",
                icon = Icons.Default.Timer,
                iconColor = GamePalPrimary,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Total Games & Completion
            HeroStatCard(
                title = "Knihovna",
                value = "$totalGames her",
                subtitle = "$completedCount dokončeno (${completionRate.roundToInt()}%)",
                icon = Icons.Default.SportsEsports,
                iconColor = GamePalSecondary,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Average Rating
            HeroStatCard(
                title = "Průměrné skóre",
                value = if (avgRating > 0) String.format(Locale.US, "%.1f / 10", avgRating) else "Nehodnoceno",
                subtitle = if (avgRating >= 8.5) "Výborný vkus! 🏆" else "Osobní hodnocení",
                icon = Icons.Default.Star,
                iconColor = RatingGold,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Completed Games
            HeroStatCard(
                title = "Dokončeno",
                value = "$completedCount titulů",
                subtitle = "${completionRate.roundToInt()}% úspěšnost dohrání",
                icon = Icons.Default.CheckCircle,
                iconColor = GamePalEmerald,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(20.dp), isDark = isDark)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = if (isDark) 0.20f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MostPlayedSpotlightCard(
    game: GameEntity,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp), isDark = isDark, accentBorder = true)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            GameCoverImage(
                coverUrl = game.coverUrl,
                contentDescription = game.title,
                modifier = Modifier.size(width = 54.dp, height = 72.dp),
                cornerRadius = 10
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GamePalPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "👑 NEJHRANĚJŠÍ TITUL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = GamePalPrimary
                        )
                    }
                    PlatformBadge(platformName = game.platform, compact = true)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = game.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ ${game.formattedTotalTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamePalSecondary
                    )

                    if (game.rating != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = RatingGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${game.rating}/10",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// Genre Breakdown Card & Donut Chart
// ---------------------------------------------------------------------------------

@Composable
private fun GenreBreakdownCard(
    genreStats: List<GenreAnalyticsItem>,
    animationProgress: Float,
    isDark: Boolean
) {
    val topGenre = genreStats.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp), isDark = isDark)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = GamePalPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Typy her & Žánry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (topGenre != null) {
                    Text(
                        text = "Top: ${topGenre.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = topGenre.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Donut Chart + Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Donut Canvas
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val strokeWidth = 24.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )

                        genreStats.forEach { item ->
                            val sweepAngle = (item.percentage / 100f * 360f) * animationProgress
                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = item.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle.coerceAtLeast(3f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${genreStats.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "žánrů",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Top 4 Genre Pills
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genreStats.take(4).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${item.count} (${item.percentage.roundToInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proportional Genre Bars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                genreStats.forEach { item ->
                    val animatedWidth = (item.percentage / 100f) * animationProgress
                    val hours = item.totalSeconds / 3600

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (hours > 0) "${item.count} her • ${hours}h" else "${item.count} her",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = animatedWidth.coerceIn(0.02f, 1f))
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(item.color, item.color.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// Platform Breakdown Card
// ---------------------------------------------------------------------------------

@Composable
private fun PlatformBreakdownCard(
    platformStats: List<PlatformAnalyticsItem>,
    animationProgress: Float,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp), isDark = isDark)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = GamePalSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Herní platformy & Hardware",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${platformStats.size} platforem",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment horizontal bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    platformStats.forEach { item ->
                        val segWeight = (item.percentage / 100f) * animationProgress
                        if (segWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(segWeight.coerceAtLeast(0.01f))
                                    .height(14.dp)
                                    .background(item.platformInfo.brandColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Platform detailed list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                platformStats.forEach { item ->
                    val hours = item.totalSeconds / 3600
                    val minutes = (item.totalSeconds % 3600) / 60

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x14FFFFFF) else Color(0x0A000000))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PlatformBadge(platformName = item.name, compact = false)
                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.count} her (${item.percentage.roundToInt()}%)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = item.platformInfo.brandColor
                            )
                            Text(
                                text = "odehráno",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// Rating Distribution Card (1 to 10 scale)
// ---------------------------------------------------------------------------------

@Composable
private fun RatingDistributionCard(
    ratingDistribution: List<Pair<Int, Int>>,
    ratedCount: Int,
    avgRating: Double,
    animationProgress: Float,
    isDark: Boolean
) {
    val maxCount = ratingDistribution.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp), isDark = isDark)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = RatingGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Rozložení hodnocení (1–10⭐)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (ratedCount > 0) {
                    Text(
                        text = "Ø ${String.format(Locale.US, "%.1f", avgRating)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = RatingGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vertical Histogram Bars (1 to 10)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                ratingDistribution.forEach { (score, count) ->
                    val barFraction = if (maxCount > 0) (count.toFloat() / maxCount) * animationProgress else 0f
                    val isHighScore = score >= 8

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighScore) RatingGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(80.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Background slot
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDark) Color(0x14FFFFFF) else Color(0x0D000000))
                            )

                            // Animated filled bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((80 * barFraction.coerceIn(0f, 1f)).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            if (isHighScore) {
                                                listOf(RatingGold, Color(0xFFF59E0B))
                                            } else {
                                                listOf(GamePalPrimary, GamePalSecondary)
                                            }
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$score",
                            fontSize = 11.sp,
                            fontWeight = if (score == 10) FontWeight.Black else FontWeight.Medium,
                            color = if (score == 10) RatingGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// Hall of Fame (Highest Rated Games 8–10)
// ---------------------------------------------------------------------------------

@Composable
private fun HallOfFameSection(
    games: List<GameEntity>,
    onGameClick: (Long) -> Unit,
    isDark: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = RatingGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Síň slávy (8–10⭐)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${games.size} klenotů",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(games) { game ->
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .glassCard(shape = RoundedCornerShape(16.dp), isDark = isDark)
                        .clickable { onGameClick(game.id) }
                        .padding(8.dp)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            GameCoverImage(
                                coverUrl = game.coverUrl,
                                contentDescription = game.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                cornerRadius = 10
                            )

                            // Rating Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RatingGold)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${game.rating}",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = game.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlatformBadge(platformName = game.platform, compact = true)
                            Text(
                                text = game.formattedTotalTime,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// Status Breakdown Card
// ---------------------------------------------------------------------------------

@Composable
private fun StatusBreakdownCard(
    playingCount: Int,
    completedCount: Int,
    backlogCount: Int,
    pausedCount: Int,
    droppedCount: Int,
    totalGames: Int,
    totalSessions: Int,
    avgSessionSec: Long,
    longestSessionSec: Long,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(shape = RoundedCornerShape(22.dp), isDark = isDark)
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "Rozložení stavů v knihovně",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Progress Items
            StatusItemRow(
                title = GameStatus.PLAYING.titleCs,
                count = playingCount,
                total = totalGames,
                color = GameStatus.PLAYING.primaryColor,
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusItemRow(
                title = GameStatus.COMPLETED.titleCs,
                count = completedCount,
                total = totalGames,
                color = GameStatus.COMPLETED.primaryColor,
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusItemRow(
                title = GameStatus.BACKLOG.titleCs,
                count = backlogCount,
                total = totalGames,
                color = GameStatus.BACKLOG.primaryColor,
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusItemRow(
                title = GameStatus.PAUSED.titleCs,
                count = pausedCount,
                total = totalGames,
                color = GameStatus.PAUSED.primaryColor,
                isDark = isDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusItemRow(
                title = GameStatus.DROPPED.titleCs,
                count = droppedCount,
                total = totalGames,
                color = GameStatus.DROPPED.primaryColor,
                isDark = isDark
            )

            if (totalSessions > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0x14FFFFFF) else Color(0x0A000000))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Průměrná relace",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${avgSessionSec / 60} min",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nejdelší relace",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${longestSessionSec / 3600}h ${(longestSessionSec % 3600) / 60}m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamePalPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusItemRow(
    title: String,
    count: Int,
    total: Int,
    color: Color,
    isDark: Boolean
) {
    val pct = if (total > 0) (count.toFloat() / total) else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "$count (${(pct * 100).roundToInt()}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = pct.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

// ---------------------------------------------------------------------------------
// Empty State
// ---------------------------------------------------------------------------------

@Composable
private fun EmptyStatsView(
    isDark: Boolean,
    onAddGameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(shape = RoundedCornerShape(26.dp), isDark = isDark, elevation = 6.dp)
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GamePalPrimary.copy(alpha = 0.25f), GamePalSecondary.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = GamePalPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Zatím žádné statistiky",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jakmile přidáte první hru a začnete měřit svůj herní čas, zobrazí se zde přehledné grafy žánrů, platformních statistik a rozložení hodnocení.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onAddGameClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePalPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Přidat první hru do knihovny", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

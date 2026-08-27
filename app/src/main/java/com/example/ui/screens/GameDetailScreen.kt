package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameEntity
import com.example.model.GameStatus
import com.example.model.PlatformInfo
import com.example.ui.components.GameCoverImage
import com.example.ui.components.PlatformBadge
import com.example.ui.components.RatingPicker
import com.example.ui.theme.GamePalPrimary
import com.example.ui.theme.GamePalSecondary
import com.example.ui.theme.RatingGold
import com.example.ui.theme.auroraBackground
import com.example.ui.theme.glassCard
import com.example.viewmodel.GamePalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameDetailScreen(
    game: GameEntity,
    viewModel: GamePalViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val sessions by viewModel.selectedGameSessions.collectAsState()

    val isTrackingThisGame = activeSession?.gameId == game.id

    var reviewText by remember(game.review) { mutableStateOf(game.review) }
    var notesText by remember(game.notes) { mutableStateOf(game.notes) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showManualTimeDialog by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showStopSessionDialog by remember { mutableStateOf(false) }
    var sessionNotesInput by remember { mutableStateOf("") }

    val currentStatus = GameStatus.fromString(game.status)

    // Gesture navigation handler: closes dialogs if open, or returns to main screen
    BackHandler {
        when {
            showDeleteConfirm -> showDeleteConfirm = false
            showManualTimeDialog -> showManualTimeDialog = false
            showStopSessionDialog -> showStopSessionDialog = false
            showStatusDropdown -> showStatusDropdown = false
            else -> onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = game.title,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(game.id) }) {
                        Icon(
                            imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Oblíbené",
                            tint = if (game.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Smazat hru",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.auroraBackground()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card: Cover Art + Main Stats
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(shape = RoundedCornerShape(24.dp), accentBorder = isTrackingThisGame)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Game Poster
                        GameCoverImage(
                            coverUrl = game.coverUrl,
                            contentDescription = game.title,
                            modifier = Modifier
                                .width(115.dp)
                                .height(160.dp),
                            cornerRadius = 16
                        )

                        // Info Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            PlatformBadge(platformName = game.platform)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status Pill with Dropdown
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(currentStatus.primaryColor.copy(alpha = 0.2f))
                                        .clickable { showStatusDropdown = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(currentStatus.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentStatus.titleCs,
                                        color = currentStatus.accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = showStatusDropdown,
                                    onDismissRequest = { showStatusDropdown = false }
                                ) {
                                    GameStatus.entries.forEach { st ->
                                        DropdownMenuItem(
                                            text = { Text(st.titleCs) },
                                            onClick = {
                                                viewModel.updateGameStatus(game.id, st.id)
                                                showStatusDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (game.developer.isNotBlank() || game.releaseYear > 0) {
                                Text(
                                    text = listOfNotNull(
                                        game.developer.ifBlank { null },
                                        if (game.releaseYear > 0) game.releaseYear.toString() else null
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Live Activity Tracking Controller
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(
                            shape = RoundedCornerShape(22.dp),
                            accentBorder = true,
                            customTint = if (isTrackingThisGame) Color(0xFF06B6D4) else null
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = if (isTrackingThisGame) GamePalSecondary else GamePalPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTrackingThisGame) "LIVE ACTIVITY AKTIVNÍ" else "MĚŘENÍ ČASU HRANÍ",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (isTrackingThisGame) GamePalSecondary else MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            if (isTrackingThisGame) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (activeSession?.isPaused == true) "POZASTAVENO" else "NA ZAMČENÉ OBRAZOVCE",
                                        color = if (activeSession?.isPaused == true) Color(0xFFF59E0B) else Color(0xFF34D399),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Play Time Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = if (isTrackingThisGame) "Aktuální relace:" else "Celkový odehraný čas:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isTrackingThisGame) activeSession?.formattedTime ?: "00:00" else game.formattedTotalTime,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!isTrackingThisGame) {
                                TextButton(
                                    onClick = { showManualTimeDialog = true }
                                ) {
                                    Icon(Icons.Default.MoreTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upravit čas", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Actions
                        if (isTrackingThisGame) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (activeSession?.isPaused == true) {
                                            viewModel.resumeSession(context)
                                        } else {
                                            viewModel.pauseSession(context)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeSession?.isPaused == true) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = if (activeSession?.isPaused == true) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (activeSession?.isPaused == true) "Pokračovat" else "Pozastavit")
                                }

                                Button(
                                    onClick = { showStopSessionDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ukončit & Uložit")
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.startSession(context, game) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GamePalPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Spustit měření hraní (Live Activity)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Rating Section (1 to 10 scale)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(shape = RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    RatingPicker(
                        currentRating = game.rating,
                        onRatingSelected = { newRating ->
                            viewModel.updateRatingAndReview(
                                gameId = game.id,
                                rating = newRating,
                                review = reviewText,
                                notes = notesText
                            )
                        }
                    )
                }
            }

            // Description / Synopsis (if available)
            if (game.description.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(shape = RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "O hře",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = game.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                            if (game.genres.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Žánry: ${game.genres}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GamePalPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Review & Notes Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(shape = RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Recenze a osobní poznámky",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            label = { Text("Moje recenze") },
                            placeholder = { Text("Napište svůj dojem ze hry, silné a slabé stránky...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Vlastní poznámky (questy, buildy, postup)") },
                            placeholder = { Text("Uložte si tipy, kde jste skončili, plány do dalšího hraní...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 5,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.updateRatingAndReview(
                                    gameId = game.id,
                                    rating = game.rating,
                                    review = reviewText,
                                    notes = notesText
                                )
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = GamePalPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Uložit texty")
                        }
                    }
                }
            }

            // Past Play Sessions History
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(shape = RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = GamePalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Historie hraní (${sessions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (sessions.isEmpty()) {
                            Text(
                                text = "Zatím žádné zaznamenané herní relace. Spusťte Live Activity výše pro první zápis!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sessions.take(8).forEach { session ->
                                    val dateStr = SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault())
                                        .format(Date(session.endTime))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (session.notes.isNotBlank()) {
                                                Text(
                                                    text = session.notes,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        Text(
                                            text = "+${session.formattedDuration}",
                                            fontWeight = FontWeight.Bold,
                                            color = GamePalSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Stop Session Dialog with Note Prompt
    if (showStopSessionDialog) {
        AlertDialog(
            onDismissRequest = { showStopSessionDialog = false },
            title = { Text("Ukončit a uložit relaci") },
            text = {
                Column {
                    Text("Zaznamenaný čas: ${activeSession?.formattedTime}")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = sessionNotesInput,
                        onValueChange = { sessionNotesInput = it },
                        label = { Text("Poznámka k relaci (nepovinné)") },
                        placeholder = { Text("Co se dnes podařilo splnit...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopSession(context, sessionNotesInput)
                        sessionNotesInput = ""
                        showStopSessionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GamePalPrimary)
                ) {
                    Text("Uložit do historie")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopSessionDialog = false }) {
                    Text("Zrušit")
                }
            }
        )
    }

    // Manual Time Dialog
    if (showManualTimeDialog) {
        var addMinutesValue by remember { mutableStateOf("60") }

        AlertDialog(
            onDismissRequest = { showManualTimeDialog = false },
            title = { Text("Přidat odehraný čas ručně") },
            text = {
                Column {
                    Text("Zvolte počet minut k přičtení k celkovému času hry:")
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 60, 120, 300).forEach { mins ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (addMinutesValue == mins.toString()) GamePalPrimary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { addMinutesValue = mins.toString() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (mins >= 60) "${mins / 60}h" else "${mins}m",
                                    color = if (addMinutesValue == mins.toString()) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = addMinutesValue,
                        onValueChange = { addMinutesValue = it },
                        label = { Text("Vlastní počet minut") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = addMinutesValue.toLongOrNull() ?: 0L
                        if (mins > 0) {
                            viewModel.addManualTime(game.id, mins)
                        }
                        showManualTimeDialog = false
                    }
                ) {
                    Text("Přičíst čas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualTimeDialog = false }) {
                    Text("Zrušit")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Odebrat hru z knihovny?") },
            text = { Text("Opravdu chcete smazat hru ${game.title}? Smažou se i všechny zaznamenané relace a hodnocení.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteGame(game.id)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Smazat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Ponechat")
                }
            }
        )
    }
}

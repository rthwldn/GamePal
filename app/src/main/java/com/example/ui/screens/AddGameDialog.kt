package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GameSearchResult
import com.example.model.GameStatus
import com.example.model.PlatformInfo
import com.example.ui.components.GameCoverImage
import com.example.ui.components.PlatformBadge
import com.example.ui.components.RatingPicker
import com.example.ui.theme.GamePalPrimary
import com.example.ui.theme.glassCard
import com.example.viewmodel.GamePalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGameDialog(
    viewModel: GamePalViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = true

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var selectedTitle by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("PlayStation 5") }
    var selectedCoverUrl by remember { mutableStateOf("") }
    var selectedDescription by remember { mutableStateOf("") }
    var selectedYear by remember { mutableIntStateOf(2024) }
    var selectedGenres by remember { mutableStateOf("") }
    var selectedDeveloper by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(GameStatus.PLAYING.id) }
    var selectedRating by remember { mutableStateOf<Int?>(null) }
    var selectedReview by remember { mutableStateOf("") }
    var selectedNotes by remember { mutableStateOf("") }
    var initialHours by remember { mutableStateOf("") }

    var isCustomEditMode by remember { mutableStateOf(false) }
    var showPlatformDropdown by remember { mutableStateOf(false) }

    val containerBg = if (isDark) Color(0xF7131722) else Color(0xFAFFFFFF)
    val inputBg = if (isDark) Color(0xFF1C2232) else Color(0xFFF1F5F9)
    val inputBorder = if (isDark) Color(0x33FFFFFF) else Color(0x22000000)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0x99000000) else Color(0x77000000))
                .clickable(onClick = onDismiss)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.96f)
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            if (isDark) listOf(Color(0x40FFFFFF), Color(0x15FFFFFF))
                            else listOf(Color(0x30000000), Color(0x10000000))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                color = containerBg,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Přidat hru do knihovny",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hledejte v databázi IGDB nebo zadejte vlastní titul",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.addGameSearchQuery.value = it
                            if (selectedTitle.isEmpty() || !isCustomEditMode) {
                                selectedTitle = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Např. Zelda, Elden Ring, Mario, Cyberpunk...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Hledat",
                                tint = GamePalPrimary
                            )
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = GamePalPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamePalPrimary,
                            unfocusedBorderColor = inputBorder,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Search Suggestions List
                        if (searchResults.isNotEmpty() && !isCustomEditMode) {
                            item {
                                Text(
                                    text = "Nalezené hry (${searchResults.size}):",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GamePalPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(searchResults) { result ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isDark) Color(0xFF1B2232) else Color(0xFFF1F5F9))
                                        .border(
                                            width = 1.dp,
                                            color = if (isDark) Color(0x22FFFFFF) else Color(0x15000000),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            selectedTitle = result.title
                                            selectedPlatform = result.platform
                                            selectedCoverUrl = result.coverUrl
                                            selectedDescription = result.description
                                            selectedYear = result.releaseYear
                                            selectedGenres = result.genres
                                            selectedDeveloper = result.developer
                                            isCustomEditMode = true
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GameCoverImage(
                                            coverUrl = result.coverUrl,
                                            contentDescription = result.title,
                                            modifier = Modifier.size(width = 48.dp, height = 64.dp),
                                            cornerRadius = 8
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = result.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                PlatformBadge(platformName = result.platform, compact = true)
                                                Text(
                                                    text = "${result.releaseYear} • ${result.genres.take(24)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(
                                    onClick = { isCustomEditMode = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Nebo zadat údaje ručně ✏️", color = GamePalPrimary)
                                }
                            }
                        }

                        // Form Fields
                        if (isCustomEditMode || searchResults.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    // Title Input
                                    OutlinedTextField(
                                        value = selectedTitle,
                                        onValueChange = { selectedTitle = it },
                                        label = { Text("Název hry *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GamePalPrimary,
                                            unfocusedBorderColor = inputBorder,
                                            focusedContainerColor = inputBg,
                                            unfocusedContainerColor = inputBg,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Platform Selector Dropdown & Quick Chips
                                    Text(
                                        text = "Herní platforma:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(inputBg)
                                            .border(1.dp, inputBorder, RoundedCornerShape(14.dp))
                                            .clickable { showPlatformDropdown = true }
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.SportsEsports,
                                                    contentDescription = null,
                                                    tint = GamePalPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = selectedPlatform,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = "Vybrat",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showPlatformDropdown,
                                            onDismissRequest = { showPlatformDropdown = false }
                                        ) {
                                            PlatformInfo.ALL_PLATFORMS.forEach { platform ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            PlatformBadge(platformName = platform.name, compact = true)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(platform.name)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedPlatform = platform.name
                                                        showPlatformDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quick platform chips
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("PS5", "Xbox Series", "Switch", "Deck", "PC", "PS4", "PS2", "SNES", "NES").forEach { pShort ->
                                            val p = PlatformInfo.findByName(pShort)
                                            val isSel = selectedPlatform.equals(p.name, true)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSel) GamePalPrimary else (if (isDark) Color(0xFF242C3E) else Color(0xFFE2E8F0))
                                                    )
                                                    .clickable { selectedPlatform = p.name }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = p.shortName,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Status Selection
                                    Text(
                                        text = "Stav v knihovně:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        GameStatus.entries.forEach { status ->
                                            val isSelected = selectedStatus == status.id
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) status.primaryColor else (if (isDark) Color(0xFF242C3E) else Color(0xFFE2E8F0))
                                                    )
                                                    .clickable { selectedStatus = status.id }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = status.titleCs,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Rating Picker 1 to 10
                                    RatingPicker(
                                        currentRating = selectedRating,
                                        onRatingSelected = { selectedRating = it }
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Initial Played Hours
                                    OutlinedTextField(
                                        value = initialHours,
                                        onValueChange = { initialHours = it },
                                        label = { Text("Již odehraný čas (hodiny)") },
                                        placeholder = { Text("např. 15") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GamePalPrimary,
                                            unfocusedBorderColor = inputBorder,
                                            focusedContainerColor = inputBg,
                                            unfocusedContainerColor = inputBg,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Short Review & Notes
                                    OutlinedTextField(
                                        value = selectedReview,
                                        onValueChange = { selectedReview = it },
                                        label = { Text("Krátká recenze") },
                                        placeholder = { Text("Váš osobní dojem ze hry...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GamePalPrimary,
                                            unfocusedBorderColor = inputBorder,
                                            focusedContainerColor = inputBg,
                                            unfocusedContainerColor = inputBg,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = selectedNotes,
                                        onValueChange = { selectedNotes = it },
                                        label = { Text("Osobní poznámky") },
                                        placeholder = { Text("Kde jsem skončil, tipy, build...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GamePalPrimary,
                                            unfocusedBorderColor = inputBorder,
                                            focusedContainerColor = inputBg,
                                            unfocusedContainerColor = inputBg,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Cover URL
                                    OutlinedTextField(
                                        value = selectedCoverUrl,
                                        onValueChange = { selectedCoverUrl = it },
                                        label = { Text("URL Cover Artu (obrázek)") },
                                        placeholder = { Text("https://...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GamePalPrimary,
                                            unfocusedBorderColor = inputBorder,
                                            focusedContainerColor = inputBg,
                                            unfocusedContainerColor = inputBg,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Zrušit", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (selectedTitle.isNotBlank()) {
                                    val hours = initialHours.toLongOrNull() ?: 0L
                                    viewModel.addGame(
                                        title = selectedTitle,
                                        platform = selectedPlatform,
                                        coverUrl = selectedCoverUrl,
                                        description = selectedDescription,
                                        releaseYear = selectedYear,
                                        genres = selectedGenres,
                                        developer = selectedDeveloper,
                                        status = selectedStatus,
                                        rating = selectedRating,
                                        review = selectedReview,
                                        notes = selectedNotes,
                                        initialPlayTimeMinutes = hours * 60
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = selectedTitle.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamePalPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Uložit do knihovny", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

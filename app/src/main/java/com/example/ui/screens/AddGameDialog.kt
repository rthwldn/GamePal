package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import com.example.util.ImageStorageUtil
import com.example.viewmodel.GamePalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGameDialog(
    viewModel: GamePalViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val localPath = ImageStorageUtil.saveCoverImageFromUri(context, uri, prefix = "cover_new")
            if (localPath != null) {
                selectedCoverUrl = localPath
            }
        }
    }

    var isCustomEditMode by remember { mutableStateOf(false) }
    var showPlatformDropdown by remember { mutableStateOf(false) }

    val containerBg = Color(0xFF0D0D12)
    val inputBg = Color(0xFF14141B)
    val inputBorder = Color(0xFF262634)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6000000))
                .clickable(onClick = onDismiss)
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x3DFFFFFF), Color(0x14FFFFFF))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                color = containerBg,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)
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
                                color = Color.White
                            )
                            Text(
                                text = "Hledejte v databázi her nebo zadejte vlastní titul",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zavřít",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar (M3 Expressive Pill)
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
                        placeholder = { Text("Např. Zelda, Elden Ring, Mario, Cyberpunk...", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Hledat",
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                        },
                        shape = RoundedCornerShape(percent = 50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = inputBorder,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF141418) else Color(0xFFF1F5F9))
                                        .border(
                                            width = 1.dp,
                                            color = if (isDark) Color(0xFF27272A) else Color(0x15000000),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedTitle = result.title
                                            val queryLower = searchQuery.lowercase()
                                            val detectedPlatform = when {
                                                queryLower.contains("switch 2") || queryLower.contains("switch2") -> "Nintendo Switch 2"
                                                queryLower.contains("ps5") || queryLower.contains("playstation 5") -> "PlayStation 5"
                                                queryLower.contains("xbox series") || queryLower.contains("xbox") -> "Xbox Series"
                                                queryLower.contains("pc") || queryLower.contains("steam") -> "PC"
                                                queryLower.contains("switch") -> "Nintendo Switch"
                                                else -> PlatformInfo.findByName(result.platform).name
                                            }
                                            selectedPlatform = detectedPlatform
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
                                            modifier = Modifier.size(width = 48.dp, height = 72.dp),
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
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(inputBg)
                                            .border(1.dp, inputBorder, RoundedCornerShape(8.dp))
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

                                    // Quick platform chips (M3 Expressive Pills)
                                    val platformPillShape = RoundedCornerShape(percent = 50)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PlatformInfo.ALL_PLATFORMS.forEach { p ->
                                            val isSel = selectedPlatform.equals(p.name, true)
                                            Box(
                                                modifier = Modifier
                                                    .clip(platformPillShape)
                                                    .background(
                                                        if (isSel) Color.White else Color(0xFF14141B)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSel) Color.White else Color(0xFF262634),
                                                        platformPillShape
                                                    )
                                                    .clickable { selectedPlatform = p.name }
                                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                            ) {
                                                Text(
                                                    text = p.shortName,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) Color.Black else Color.White
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Status Selection (M3 Expressive Pills)
                                    Text(
                                        text = "Stav v knihovně:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val statusPillShape = RoundedCornerShape(percent = 50)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        GameStatus.entries.forEach { status ->
                                            val isSelected = selectedStatus == status.id
                                            Box(
                                                modifier = Modifier
                                                    .clip(statusPillShape)
                                                    .background(
                                                        if (isSelected) status.primaryColor else Color(0xFF14141B)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) status.primaryColor else Color(0xFF262634),
                                                        statusPillShape
                                                    )
                                                    .clickable { selectedStatus = status.id }
                                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                            ) {
                                                Text(
                                                    text = status.titleCs,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
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
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
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
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
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
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
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
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = inputBorder,
                                            focusedContainerColor = inputBg,
                                            unfocusedContainerColor = inputBg,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1E1E28),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(percent = 50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Vybrat obrázek z telefonu (Galerie)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (selectedCoverUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                 .fillMaxWidth()
                                                 .clip(RoundedCornerShape(12.dp))
                                                 .background(Color(0xFF14141B))
                                                 .border(1.dp, Color(0xFF262634), RoundedCornerShape(12.dp))
                                                 .padding(8.dp)
                                         ) {
                                             GameCoverImage(
                                                 coverUrl = selectedCoverUrl,
                                                 contentDescription = "Náhled",
                                                 modifier = Modifier.size(width = 36.dp, height = 54.dp),
                                                 cornerRadius = 6
                                             )
                                             Spacer(modifier = Modifier.width(10.dp))
                                             Text(
                                                 text = "Obal vybrán ✓",
                                                 fontSize = 12.sp,
                                                 fontWeight = FontWeight.Bold,
                                                 color = Color.White,
                                                 modifier = Modifier.weight(1f)
                                             )
                                             TextButton(onClick = { selectedCoverUrl = "" }) {
                                                 Text("Odebrat", color = Color(0xFFEF4444), fontSize = 11.sp)
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }

                     Spacer(modifier = Modifier.height(10.dp))

                     // Bottom Action Buttons (M3 Expressive Pills)
                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(top = 8.dp, bottom = 12.dp),
                         horizontalArrangement = Arrangement.End,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         TextButton(
                             onClick = onDismiss,
                             modifier = Modifier.height(48.dp)
                         ) {
                             Text("Zrušit", color = Color(0xFF94A3B8))
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
                                 containerColor = Color.White,
                                 contentColor = Color.Black,
                                 disabledContainerColor = Color(0xFF1E1E26),
                                 disabledContentColor = Color(0xFF64748B)
                             ),
                             shape = RoundedCornerShape(percent = 50),
                             modifier = Modifier
                                 .height(48.dp)
                                 .testTag("add_game_submit_button")
                         ) {
                             Icon(
                                 imageVector = Icons.Default.Add,
                                 contentDescription = null,
                                 tint = if (selectedTitle.isNotBlank()) Color.Black else Color(0xFF64748B),
                                 modifier = Modifier.size(18.dp)
                             )
                             Spacer(modifier = Modifier.width(6.dp))
                             Text("Přidat do knihovny", fontWeight = FontWeight.Bold)
                         }
                     }
                }
            }
        }
    }
}

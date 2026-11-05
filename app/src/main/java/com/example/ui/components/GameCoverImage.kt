package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun GameCoverImage(
    coverUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 8
) {
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius.dp) }
    val context = LocalContext.current
    val imageRequest = remember(coverUrl) {
        if (coverUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(coverUrl)
                .crossfade(100)
                .build()
        } else null
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF141418))
            .border(0.5.dp, Color(0x1FFFFFFF), shape),
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = Color(0xFF71717A),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

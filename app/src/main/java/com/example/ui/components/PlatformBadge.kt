package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlatformInfo

@Composable
fun PlatformBadge(
    platformName: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val platformInfo = PlatformInfo.findByName(platformName)
    val displayText = if (compact) platformInfo.shortName else platformInfo.name

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(platformInfo.brandColor.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = platformInfo.brandColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 4.dp)
    ) {
        Text(
            text = displayText,
            color = platformInfo.accentColor,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

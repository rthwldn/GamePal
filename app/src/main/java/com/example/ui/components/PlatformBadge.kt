package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val pillShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(platformInfo.brandColor.copy(alpha = 0.25f))
            .border(
                width = 1.dp,
                color = platformInfo.brandColor.copy(alpha = 0.65f),
                shape = pillShape
            )
            .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 2.dp else 4.dp)
    ) {
        Text(
            text = displayText,
            color = platformInfo.accentColor,
            fontSize = if (compact) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

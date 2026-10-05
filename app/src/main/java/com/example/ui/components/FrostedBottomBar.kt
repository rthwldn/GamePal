package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MainNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Domů", Icons.Default.SportsEsports, Icons.Outlined.SportsEsports),
    STATS("Statistiky", Icons.Default.BarChart, Icons.Outlined.BarChart)
}

@Composable
fun FrostedBottomBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val barShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x7709090B),
                        Color(0xD909090B),
                        Color(0xFF09090B)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = barShape,
                    spotColor = Color(0xCC000000),
                    ambientColor = Color(0x66000000)
                )
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF518181D), // Top specular matte
                            Color(0xF8121215), // Mid body
                            Color(0xFF09090B)  // Bottom depth
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF), // Crisp top highlight
                            Color(0x10FFFFFF)  // Subtle bottom rim
                        )
                    ),
                    shape = barShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Domů
                NavTabItem(
                    tab = MainNavTab.HOME,
                    isSelected = currentTab == MainNavTab.HOME,
                    onClick = { onTabSelected(MainNavTab.HOME) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                // Middle Action: Modern Industrial Add Button (Crisp White Squircle)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAddClick
                        )
                        .testTag("bottom_add_game_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Přidat hru",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Tab 2: Statistiky
                NavTabItem(
                    tab = MainNavTab.STATS,
                    isSelected = currentTab == MainNavTab.STATS,
                    onClick = { onTabSelected(MainNavTab.STATS) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: MainNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tabShape = RoundedCornerShape(8.dp)
    val pillBg = if (isSelected) {
        if (isDark) Color(0x1FFFFFFF) else Color(0x14000000)
    } else Color.Transparent

    val iconTint = if (isSelected) Color.White else {
        if (isDark) Color(0xFF71717A) else Color(0xFF71717A)
    }

    val textColor = if (isSelected) {
        if (isDark) Color.White else Color(0xFF09090B)
    } else {
        if (isDark) Color(0xFF71717A) else Color(0xFF71717A)
    }

    val pillPaddingHorizontal by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 6.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PillPadding"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(tabShape)
            .background(pillBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("nav_tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = pillPaddingHorizontal, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = tab.title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

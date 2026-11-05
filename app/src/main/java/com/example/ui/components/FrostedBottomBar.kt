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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.ExpressiveCyanGlow
import com.example.ui.theme.ExpressiveElectricIndigo

enum class MainNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Domů", Icons.Default.SportsEsports, Icons.Outlined.SportsEsports),
    STATS("Statistiky", Icons.Default.BarChart, Icons.Outlined.BarChart)
}

// Fade to pure OLED black #000000
private val BottomBarScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x88000000),
        Color(0xEE000000),
        Color(0xFF000000)
    )
)

private val BottomBarBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xF5171720),
        Color(0xF8111116),
        Color(0xFF09090D)
    )
)

private val BottomBarBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x3DFFFFFF),
        Color(0x18FFFFFF)
    )
)

@Composable
fun FrostedBottomBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    // Material 3 Expressive Pill Shape
    val barShape = remember { RoundedCornerShape(percent = 50) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomBarScrimBrush)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = barShape,
                    spotColor = Color(0xFF000000),
                    ambientColor = Color(0x99000000)
                )
                .clip(barShape)
                .background(BottomBarBackgroundBrush)
                .border(
                    width = 1.dp,
                    brush = BottomBarBorderBrush,
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

                // Middle Action: Expressive Tactile Action Button (Pill / Circle)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0))
                            )
                        )
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
                        modifier = Modifier.size(24.dp)
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
    // Material 3 Expressive Active Pill Indicator
    val pillShape = remember { RoundedCornerShape(percent = 50) }
    val pillBg = if (isSelected) {
        if (isDark) Color(0x28FFFFFF) else Color(0x14000000)
    } else Color.Transparent

    val iconTint = if (isSelected) Color.White else Color(0xFF94A3B8)
    val textColor = if (isSelected) Color.White else Color(0xFF94A3B8)

    val pillPaddingHorizontal by animateDpAsState(
        targetValue = if (isSelected) 14.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PillPadding"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(pillShape)
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

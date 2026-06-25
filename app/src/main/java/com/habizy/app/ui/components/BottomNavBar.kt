package com.habizy.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightText

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Signaler", Icons.Filled.Flag, Icons.Outlined.Flag),
    BottomNavItem("Ménage", Icons.Filled.CleaningServices, Icons.Outlined.CleaningServices),
    BottomNavItem("Dépenses", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person),
)

/**
 * Custom bottom navigation bar with an animated green pill indicator on the active tab,
 * rounded top corners, and a top shadow -- matching the iOS CustomTabBar.
 */
@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 10.dp, bottom = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                BottomNavTab(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) GreenPrimary else LightText,
        animationSpec = tween(250),
        label = "tabIconColor",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) GreenPrimary else LightText,
        animationSpec = tween(200),
        label = "tabTextColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "tabScale",
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.12f else 0f,
        animationSpec = tween(250),
        label = "pillAlpha",
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Pill background behind icon
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(GreenPrimary.copy(alpha = pillAlpha)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale),
            )
        }

        Text(
            text = item.label,
            fontFamily = DmSansFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp,
            color = textColor,
        )
    }
}

package com.habizy.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.ui.components.AnimatedCounter
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.ShimmerHomeLoading
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenDark
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.Orange
import com.habizy.app.ui.theme.Purple
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.HomeData
import com.habizy.app.ui.viewmodel.HomeState
import com.habizy.app.ui.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToRotation: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMenage: () -> Unit,
    onNavigateToReportDetail: (String) -> Unit,
) {
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        when (val currentState = state) {
            is HomeState.Loading -> {
                ShimmerHomeLoading(
                    modifier = Modifier.padding(top = 60.dp),
                )
            }

            is HomeState.Error -> {
                ErrorView(
                    message = currentState.message,
                    onRetry = { viewModel.load() },
                )
            }

            is HomeState.NoColocation -> {
                SetupColocationView(
                    onCreateColocation = { name -> viewModel.createColocation(name) },
                )
            }

            is HomeState.Loaded -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LoadedContent(
                        data = currentState.data,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToRotation = onNavigateToRotation,
                        onNavigateToShopping = onNavigateToShopping,
                        onNavigateToStats = onNavigateToStats,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToMenage = onNavigateToMenage,
                        onNavigateToReportDetail = onNavigateToReportDetail,
                    )
                }
            }
        }
    }
}

// -- Error view --

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            fontFamily = DmSansFamily,
            fontSize = 16.sp,
            color = CoralRed,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Reessayer",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

// -- Setup / No colocation view --

@Composable
private fun SetupColocationView(
    onCreateColocation: (String) -> Unit,
) {
    var colocationName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 80.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Welcome icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GreenPrimary, GreenDark),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bienvenue !",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = DarkText,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cree ta colocation pour commencer",
            fontFamily = DmSansFamily,
            fontSize = 15.sp,
            color = SubtitleText,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Create colocation form in a card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(CardBackground)
                .padding(20.dp),
        ) {
            Text(
                text = "Nom de la colocation",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = DarkText,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = colocationName,
                onValueChange = { colocationName = it },
                placeholder = {
                    Text(
                        text = "Ex: Appart rue de la Paix",
                        fontFamily = DmSansFamily,
                        color = SubtitleText,
                    )
                },
                textStyle = TextStyle(
                    fontFamily = DmSansFamily,
                    fontSize = 16.sp,
                    color = DarkText,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = GreenPrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onCreateColocation(colocationName.trim()) },
                enabled = colocationName.isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp)),
            ) {
                Text(
                    text = "Creer",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

// -- Loaded content --

@Composable
private fun LoadedContent(
    data: HomeData,
    onNavigateToNotifications: () -> Unit,
    onNavigateToRotation: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMenage: () -> Unit,
    onNavigateToReportDetail: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        // -- Header row --
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Bonjour,",
                    fontFamily = DmSansFamily,
                    fontSize = 15.sp,
                    color = SubtitleText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.userName,
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = DarkText,
                )
            }

            // Notification bell
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(4.dp, RoundedCornerShape(15.dp))
                    .clip(RoundedCornerShape(15.dp))
                    .background(CardBackground)
                    .clickable { onNavigateToNotifications() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = DarkText,
                    modifier = Modifier.size(22.dp),
                )
                // Red dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CoralRed),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -- Spending card --
        SpendingCard(
            totalSpent = data.totalSpent,
            memberCount = data.memberCount,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // -- Stats row --
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Mon tour
            StatMiniCard(
                title = "Mon tour",
                value = data.daysUntilTurn,
                valueColor = DarkText,
                modifier = Modifier.weight(1f),
            )
            // Mes depenses
            StatMiniCard(
                title = "Mes depenses",
                value = "-${formatEuro(data.mySpent)}",
                valueColor = CoralRed,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -- Current shopper card --
        if (data.isMyTurn) {
            MyTurnCard(onClick = onNavigateToRotation)
        } else if (data.currentShopperName.isNotBlank()) {
            CurrentShopperCard(
                shopperName = data.currentShopperName,
                shopperColorHex = data.currentShopperColor ?: "#888888",
                shopperInitial = data.currentShopperInitial ?: data.currentShopperName.take(1).uppercase(),
                onClick = onNavigateToRotation,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -- Shopping preview card --
        if (data.shoppingItemCount > 0 || data.shoppingPreview.isNotEmpty()) {
            ShoppingPreviewCard(
                items = data.shoppingPreview.map { it.name },
                totalCount = data.shoppingItemCount,
                onClick = onNavigateToShopping,
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // -- Quick actions grid --
        QuickActionsGrid(
            onNavigateToStats = onNavigateToStats,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToMenage = onNavigateToMenage,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -- Recent reports --
        if (data.recentReports.isNotEmpty()) {
            Text(
                text = "Signalements recents",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = DarkText,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                data.recentReports.forEach { report ->
                    ReportMiniCard(
                        title = report.title,
                        authorName = report.user.name,
                        authorColorHex = report.user.colorHex ?: "#888888",
                        authorInitial = report.user.initial ?: report.user.name.take(1).uppercase(),
                        commentCount = report.commentCount ?: 0,
                        timeAgo = report.createdAt?.let { formatTimeAgo(it) } ?: "",
                        hasPhoto = !report.photoUrls.isNullOrEmpty(),
                        onClick = { onNavigateToReportDetail(report.id) },
                    )
                }
            }
        }
    }
}

// -- Spending card --

@Composable
private fun SpendingCard(
    totalSpent: Double,
    memberCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GreenPrimary, GreenDark),
                ),
            )
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = "Depenses de la coloc",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedCounter(
                targetValue = totalSpent,
                style = TextStyle(
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                ),
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$memberCount membres",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

// -- Stat mini card --

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(16.dp),
    ) {
        Text(
            text = title,
            fontFamily = DmSansFamily,
            fontSize = 13.sp,
            color = SubtitleText,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = valueColor,
        )
    }
}

// -- My turn card (orange gradient) --

@Composable
private fun MyTurnCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Orange, Color(0xFFFF9800)),
                ),
            )
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "C'est ton tour !",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tu es le prochain a faire les courses",
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

// -- Current shopper card (blue gradient) --

@Composable
private fun CurrentShopperCard(
    shopperName: String,
    shopperColorHex: String,
    shopperInitial: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Blue, Color(0xFF2563EB)),
                ),
            )
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoommateAvatar(
                colorHex = shopperColorHex,
                initial = shopperInitial,
                size = 44.dp,
                cornerRadius = 15.dp,
                fontSize = 17.sp,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tour actuel",
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = shopperName,
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// -- Shopping preview card --

@Composable
private fun ShoppingPreviewCard(
    items: List<String>,
    totalCount: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(CardBackground)
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Articles manquants",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = DarkText,
            )
            Text(
                text = "$totalCount",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GreenPrimary,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        items.forEach { itemName ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = itemName,
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (totalCount > items.size) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Voir les ${totalCount - items.size} autres",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = GreenPrimary,
            )
        }
    }
}

// -- Quick actions grid --

@Composable
private fun QuickActionsGrid(
    onNavigateToStats: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMenage: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                icon = Icons.Default.BarChart,
                label = "Statistiques",
                color = Purple,
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                icon = Icons.Default.History,
                label = "Historique",
                color = Blue,
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionCard(
                icon = Icons.Default.CleaningServices,
                label = "Menage",
                color = Orange,
                onClick = onNavigateToMenage,
                modifier = Modifier.weight(1f),
            )
            // Empty spacer to maintain 2-column grid
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = DarkText,
        )
    }
}

// -- Report mini card --

@Composable
private fun ReportMiniCard(
    title: String,
    authorName: String,
    authorColorHex: String,
    authorInitial: String,
    commentCount: Int,
    timeAgo: String,
    hasPhoto: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        if (hasPhoto) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BorderColor.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = SubtitleText,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = title,
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DarkText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoommateAvatar(
                colorHex = authorColorHex,
                initial = authorInitial,
                size = 22.dp,
                cornerRadius = 7.dp,
                fontSize = 10.sp,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = authorName,
                fontFamily = DmSansFamily,
                fontSize = 12.sp,
                color = SubtitleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (commentCount > 0) {
                Text(
                    text = "$commentCount commentaire${if (commentCount > 1) "s" else ""}",
                    fontFamily = DmSansFamily,
                    fontSize = 11.sp,
                    color = SubtitleText,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = timeAgo,
                fontFamily = DmSansFamily,
                fontSize = 11.sp,
                color = SubtitleText,
            )
        }
    }
}

// -- Helpers --

private fun formatEuro(value: Double): String {
    val formatted = String.format(Locale.FRANCE, "%.2f", value).replace('.', ',')
    return "$formatted €"
}

private fun formatTimeAgo(isoDate: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.FRANCE)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoDate) ?: return isoDate
        val now = System.currentTimeMillis()
        val diffMs = now - date.time
        val diffMin = diffMs / 60_000
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24

        when {
            diffMin < 1 -> "a l'instant"
            diffMin < 60 -> "il y a ${diffMin}min"
            diffHours < 24 -> "il y a ${diffHours}h"
            diffDays < 7 -> "il y a ${diffDays}j"
            else -> {
                val displayFmt = java.text.SimpleDateFormat("dd MMM", Locale.FRANCE)
                displayFmt.format(date)
            }
        }
    } catch (_: Exception) {
        isoDate
    }
}

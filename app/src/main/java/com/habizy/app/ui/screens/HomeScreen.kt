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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.data.model.ReportResponse
import com.habizy.app.data.model.ShoppingItemResponse
import com.habizy.app.ui.components.AnimatedCounter
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.ShimmerHomeLoading
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenDark
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.Orange
import com.habizy.app.ui.theme.Purple
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.theme.tagColor
import com.habizy.app.ui.theme.toComposeColor
import com.habizy.app.ui.viewmodel.HomeData
import com.habizy.app.ui.viewmodel.HomeState
import com.habizy.app.ui.viewmodel.HomeViewModel
import com.habizy.app.ui.viewmodel.MenageHomeData
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
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if (state == Lifecycle.State.RESUMED) viewModel.silentRefresh()
        }
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
                text = "Réessayer",
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
            text = "Crée ta colocation pour commencer",
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
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text(
                    text = "Créer",
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

        // -- Les courses card --
        LesCoursesCard(
            data = data,
            onNavigateToRotation = onNavigateToRotation,
            onNavigateToShopping = onNavigateToShopping,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToStats = onNavigateToStats,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // -- Ménage section --
        MenageSection(menage = data.menage, onClick = onNavigateToMenage)

        // -- Signalements section --
        if (data.recentReports.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            SignalementsSection(
                reports = data.recentReports,
                onNavigateToReportDetail = onNavigateToReportDetail,
            )
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
                text = "Dépenses de la coloc",
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

// -- Les courses card (Mon tour + Mes dépenses + shopping) --

@Composable
private fun LesCoursesCard(
    data: HomeData,
    onNavigateToRotation: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(CardBackground),
    ) {
        // Header
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.ShoppingCart, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Les courses",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = DarkText,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))

        // Stat boxes: Mon tour + Mes dépenses
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ScreenBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text("Mon tour", fontFamily = DmSansFamily, fontSize = 12.sp, color = SubtitleText)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (data.isUserDisabled) "Désactivé" else data.daysUntilTurn.ifEmpty { "—" },
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (data.isUserDisabled) SubtitleText else DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ScreenBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text("Mes dépenses", fontFamily = DmSansFamily, fontSize = 12.sp, color = SubtitleText)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "-${formatEuro(data.mySpent)}",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CoralRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Turn indicator
        if (data.isMyTurn) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Orange, Color(0xFFFF9800))))
                    .clickable { onNavigateToRotation() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("C'est ton tour !", fontFamily = FredokaFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text("Tu es le prochain a faire les courses", fontFamily = DmSansFamily, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            }
        } else if (data.currentShopperName.isNotBlank()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Blue, Color(0xFF2563EB))))
                    .clickable { onNavigateToRotation() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoommateAvatar(
                    colorHex = data.currentShopperColor ?: "#888888",
                    initial = data.currentShopperInitial ?: data.currentShopperName.take(1).uppercase(),
                    size = 40.dp,
                    cornerRadius = 13.dp,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Aux courses", fontFamily = DmSansFamily, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(data.currentShopperName, fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ScreenBackground)
                    .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                    .clickable { onNavigateToRotation() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(DividerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Refresh, null, tint = SubtitleText, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ordre non configuré", fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkText)
                    Text("Configurer l'ordre de passage", fontFamily = DmSansFamily, fontSize = 12.sp, color = SubtitleText)
                }
                Icon(Icons.Default.ChevronRight, null, tint = SubtitleText, modifier = Modifier.size(22.dp))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = DividerColor)

        // Shopping preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToShopping() }
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Articles manquants", fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DarkText)
                Text("${data.shoppingItemCount} à acheter", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GreenPrimary)
            }
            if (data.shoppingPreview.isEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Aucun article manquant", fontFamily = DmSansFamily, fontSize = 13.sp, color = SubtitleText)
            } else {
                Spacer(Modifier.height(10.dp))
                data.shoppingPreview.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(GreenPrimary))
                        Spacer(Modifier.width(10.dp))
                        Text(item.name, fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = DarkText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text("x${item.quantity}", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SubtitleText)
                    }
                }
                val remaining = data.shoppingItemCount - data.shoppingPreview.size
                if (remaining > 0) {
                    Spacer(Modifier.height(10.dp))
                    Text("Voir les $remaining autres articles", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GreenPrimary)
                }
            }
        }

        // Last receipt (if just completed a turn)
        if (data.lastMyReceipt != null) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = DividerColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToHistory() }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(GreenPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ticket enregistré", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DarkText)
                    Text("${data.lastMyReceipt.store} · ${formatEuro(data.lastMyReceipt.totalAmount)}", fontFamily = DmSansFamily, fontSize = 12.sp, color = SubtitleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.ChevronRight, null, tint = SubtitleText, modifier = Modifier.size(18.dp))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = DividerColor)

        // Statistiques + Historique buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CourseActionButton(icon = Icons.Default.BarChart, label = "Statistiques", color = Purple, onClick = onNavigateToStats, modifier = Modifier.weight(1f))
            CourseActionButton(icon = Icons.Default.History, label = "Historique", color = Blue, onClick = onNavigateToHistory, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CourseActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
    }
}

// -- Ménage section card --

@Composable
private fun MenageSection(
    menage: MenageHomeData?,
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
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Orange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CleaningServices, null, tint = Orange, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Ménage", fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = DarkText)
                Text("Cette semaine", fontFamily = DmSansFamily, fontSize = 12.sp, color = SubtitleText)
            }
            Icon(Icons.Default.ChevronRight, null, tint = SubtitleText, modifier = Modifier.size(20.dp))
        }

        if (menage != null) {
            Spacer(Modifier.height(16.dp))

            // Personal status pill
            val done = menage.myDone
            val statusColor = if (done) GreenPrimary else Orange
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (done) "Tu as fait ton ménage !" else "À faire cette semaine",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = statusColor,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${menage.doneCount}/${menage.totalCount}",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = statusColor,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progression label + count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Progression de la coloc", fontFamily = DmSansFamily, fontSize = 12.sp, color = SubtitleText)
                Text(
                    "${menage.doneCount} sur ${menage.totalCount}",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = DarkText,
                )
            }
            Spacer(Modifier.height(8.dp))

            // Progress bar
            val progress = if (menage.totalCount > 0) menage.doneCount.toFloat() / menage.totalCount else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DividerColor),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GreenPrimary),
                )
            }

            // Member avatars
            if (menage.board.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    menage.board.take(7).forEach { member ->
                        Box(contentAlignment = Alignment.BottomEnd) {
                            RoommateAvatar(
                                colorHex = member.colorHex ?: "#888888",
                                initial = member.initial ?: member.name.take(1).uppercase(),
                                size = 38.dp,
                                cornerRadius = 12.dp,
                                fontSize = 14.sp,
                            )
                            // Status dot
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(CardBackground)
                                    .padding(2.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(if (member.done) GreenPrimary else Color(0xFFDDDDDD)),
                                )
                            }
                        }
                    }
                    val overflow = menage.board.size - minOf(7, menage.board.size)
                    if (overflow > 0) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DividerColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("+$overflow", fontFamily = DmSansFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SubtitleText)
                        }
                    }
                }
            }

            // Task description
            if (!menage.taskDescription.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, null, tint = SubtitleText, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = menage.taskDescription,
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = SubtitleText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// -- Signalements section card --

@Composable
private fun SignalementsSection(
    reports: List<ReportResponse>,
    onNavigateToReportDetail: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(CardBackground),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CoralRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Flag, null, tint = CoralRed, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("Signalements récents", fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = DarkText)
        }

        Spacer(Modifier.height(4.dp))

        reports.forEachIndexed { index, report ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReportDetail(report.id) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val firstPhoto = report.photoUrls?.firstOrNull()
                if (firstPhoto != null) {
                    AsyncImage(
                        model = firstPhoto,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(CoralRed.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Flag, null, tint = CoralRed, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(report.title, fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DarkText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val tagDetails = report.tagDetails?.take(3)
                        ?: report.tags?.take(3)?.map { com.habizy.app.data.model.TagDetail(it) }
                    if (!tagDetails.isNullOrEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            tagDetails.forEach { detail ->
                                val chipColor = detail.color
                                    ?.let { runCatching { it.toComposeColor() }.getOrNull() }
                                    ?: tagColor(detail.title)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(chipColor.copy(alpha = 0.13f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(detail.title, fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = chipColor)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${report.user.name} · ${report.createdAt?.let { formatTimeAgo(it) } ?: ""}",
                        fontFamily = DmSansFamily,
                        fontSize = 12.sp,
                        color = SubtitleText,
                    )
                }
                Icon(Icons.Default.ChevronRight, null, tint = SubtitleText, modifier = Modifier.size(18.dp))
            }
            if (index < reports.lastIndex) {
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
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
            diffMin < 1 -> "à l'instant"
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

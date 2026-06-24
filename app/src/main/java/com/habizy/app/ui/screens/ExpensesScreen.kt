package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.ShimmerExpensesLoading
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.BodyText
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenLight
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.InfoBg
import com.habizy.app.ui.theme.InfoText
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.ExpensesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    onNavigateToArticleStats: () -> Unit,
    viewModel: ExpensesViewModel = viewModel(),
) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isMyTurn by viewModel.isMyTurn.collectAsStateWithLifecycle()
    val currentPurchaserName by viewModel.currentPurchaserName.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var selectedReceipt by remember { mutableStateOf<ReceiptResponse?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.ERROR)
                viewModel.clearErrorMessage()
            }
        }
    }

    // Show success snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.SUCCESS)
                viewModel.clearSuccessMessage()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        if (isLoading && receipts.isEmpty() && stats == null) {
            ShimmerExpensesLoading(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
            ) {
                // Header
                Text(
                    text = "Depenses",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = DarkText,
                )

                Spacer(Modifier.height(16.dp))

                // Info banner when not my turn
                if (!isMyTurn && currentPurchaserName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(InfoBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = InfoText,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "C'est au tour de $currentPurchaserName de faire les courses",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = InfoText,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Stats section
                if (stats != null) {
                    val expenseStats = stats!!

                    // Total card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .padding(20.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Total depense",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = SubtitleText,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = String.format("%.2f €", expenseStats.totalSpent)
                                        .replace(".", ","),
                                    fontFamily = FredokaFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = DarkText,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GreenPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Category breakdown
                    if (expenseStats.byCategory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardBackground)
                                .padding(20.dp),
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Par categorie",
                                        fontFamily = FredokaFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = DarkText,
                                    )
                                    Text(
                                        text = "Voir les articles",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = GreenPrimary,
                                        modifier = Modifier.clickable {
                                            onNavigateToArticleStats()
                                        },
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                expenseStats.byCategory.forEach { cat ->
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = cat.category,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = BodyText,
                                            )
                                            Text(
                                                text = String.format("%.2f €", cat.total)
                                                    .replace(".", ","),
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = DarkText,
                                            )
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { cat.fraction.toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = GreenPrimary,
                                            trackColor = GreenLight.copy(alpha = 0.18f),
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                    }

                    // Roommate bars
                    if (expenseStats.byRoommate.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardBackground)
                                .padding(20.dp),
                        ) {
                            Column {
                                Text(
                                    text = "Par colocataire",
                                    fontFamily = FredokaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = DarkText,
                                )

                                Spacer(Modifier.height(14.dp))

                                expenseStats.byRoommate.forEach { roommate ->
                                    val user = roommate.user
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (user != null) {
                                            RoommateAvatar(
                                                colorHex = user.colorHex ?: "#888888",
                                                initial = user.initial ?: user.name.take(1)
                                                    .uppercase(),
                                                size = 32.dp,
                                                cornerRadius = 10.dp,
                                                fontSize = 13.sp,
                                            )
                                            Spacer(Modifier.width(10.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = user?.name ?: "Inconnu",
                                                    fontFamily = DmSansFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = BodyText,
                                                )
                                                Text(
                                                    text = String.format(
                                                        "%.2f €",
                                                        roommate.total
                                                    ).replace(".", ","),
                                                    fontFamily = DmSansFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = DarkText,
                                                )
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = {
                                                    roommate.fraction.toFloat().coerceIn(0f, 1f)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = GreenPrimary,
                                                trackColor = GreenLight.copy(alpha = 0.18f),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                    }
                }

                // Receipts section
                if (receipts.isNotEmpty()) {
                    Text(
                        text = "Tickets de caisse",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = DarkText,
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground),
                    ) {
                        Column {
                            receipts.forEachIndexed { index, receipt ->
                                ReceiptRow(
                                    receipt = receipt,
                                    onClick = { selectedReceipt = receipt },
                                )
                                if (index < receipts.lastIndex) {
                                    HorizontalDivider(
                                        color = DividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt detail bottom sheet
    if (selectedReceipt != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedReceipt = null },
            sheetState = sheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            ReceiptDetailSheet(
                receipt = selectedReceipt!!,
                isAdmin = isAdmin,
                onDelete = { receiptId ->
                    viewModel.deleteReceipt(receiptId)
                    selectedReceipt = null
                },
            )
        }
    }
}

@Composable
private fun ReceiptRow(
    receipt: ReceiptResponse,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoommateAvatar(
            colorHex = receipt.user.colorHex ?: "#888888",
            initial = receipt.user.initial ?: receipt.user.name.take(1).uppercase(),
            size = 40.dp,
            cornerRadius = 13.dp,
            fontSize = 15.sp,
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = receipt.store,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = receipt.user.name,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = SubtitleText,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%.2f €", receipt.totalAmount).replace(".", ","),
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = CoralRed,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = receipt.date,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = LightText,
            )
        }

        Spacer(Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = LightText,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ReceiptDetailSheet(
    receipt: ReceiptResponse,
    isAdmin: Boolean,
    onDelete: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        // Photo
        if (!receipt.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = receipt.photoUrl,
                contentDescription = "Photo du ticket",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(16.dp))
        }

        // Store + total header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = receipt.store,
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = DarkText,
            )
            Text(
                text = String.format("%.2f €", receipt.totalAmount).replace(".", ","),
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = CoralRed,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Date + user meta
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoommateAvatar(
                colorHex = receipt.user.colorHex ?: "#888888",
                initial = receipt.user.initial ?: receipt.user.name.take(1).uppercase(),
                size = 28.dp,
                cornerRadius = 9.dp,
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = receipt.user.name,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = BodyText,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = buildString {
                    append(receipt.date)
                    if (!receipt.time.isNullOrBlank()) {
                        append(" a ")
                        append(receipt.time)
                    }
                },
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = SubtitleText,
            )
        }

        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = DividerColor)
        Spacer(Modifier.height(14.dp))

        // Items list
        if (receipt.items.isNotEmpty()) {
            Text(
                text = "Articles",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = DarkText,
            )
            Spacer(Modifier.height(10.dp))

            receipt.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = DarkText,
                        )
                        Text(
                            text = item.category,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = SubtitleText,
                        )
                    }
                    Text(
                        text = if (item.quantity > 1) "x${item.quantity}" else "",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = LightText,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    Text(
                        text = String.format("%.2f €", item.price).replace(".", ","),
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DarkText,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor)
        }

        // Admin delete button
        if (isAdmin) {
            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { onDelete(receipt.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed.copy(alpha = 0.12f),
                    contentColor = CoralRed,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Supprimer le ticket",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

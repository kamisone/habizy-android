package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.BodyText
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightCardBg
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.ReceiptViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(
    onBack: () -> Unit,
    viewModel: ReceiptViewModel = viewModel(),
) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Historique", onBack = onBack)

        if (isLoading && receipts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (receipts.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = LightText,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Aucun achat enregistre",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = DarkText,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground),
                ) {
                    Column {
                        receipts.forEachIndexed { index, receipt ->
                            HistoryReceiptRow(
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

                Spacer(Modifier.height(24.dp))
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
            HistoryReceiptDetailSheet(receipt = selectedReceipt!!)
        }
    }
}

@Composable
private fun HistoryReceiptRow(
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
    }
}

@Composable
private fun HistoryReceiptDetailSheet(
    receipt: ReceiptResponse,
) {
    var showFullscreen by remember { mutableStateOf(false) }

    // Fullscreen photo viewer
    if (showFullscreen && receipt.photoUrl != null) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                if (scale > 1f) {
                    offsetX += panChange.x
                    offsetY += panChange.y
                } else {
                    offsetX = 0f
                    offsetY = 0f
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                AsyncImage(
                    model = receipt.photoUrl,
                    contentDescription = "Photo du ticket",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                        )
                        .transformable(state = transformableState),
                )
                IconButton(
                    onClick = { showFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        // Header: photo if available, otherwise icon
        if (receipt.photoUrl != null) {
            AsyncImage(
                model = receipt.photoUrl,
                contentDescription = "Photo du ticket",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showFullscreen = true },
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GreenPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Store name
        Text(
            text = receipt.store,
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = DarkText,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(6.dp))

        // "par {user} · {date}"
        Text(
            text = "par ${receipt.user.name} · ${receipt.date}",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = SubtitleText,
        )

        Spacer(Modifier.height(18.dp))

        // Total card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GreenPrimary.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = GreenPrimary,
                )
                Text(
                    text = String.format("%.2f €", receipt.totalAmount).replace(".", ","),
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = GreenPrimary,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Articles list
        if (receipt.items.isNotEmpty()) {
            Text(
                text = "Articles",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = DarkText,
            )

            Spacer(Modifier.height(10.dp))

            receipt.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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

                    if (item.quantity > 1) {
                        Text(
                            text = "x${item.quantity}",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = LightText,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }

                    Text(
                        text = String.format("%.2f €", item.price).replace(".", ","),
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DarkText,
                    )
                }

                if (index < receipt.items.lastIndex) {
                    HorizontalDivider(color = DividerColor)
                }
            }
        }
    }
}

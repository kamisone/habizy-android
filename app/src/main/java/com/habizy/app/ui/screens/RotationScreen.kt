package com.habizy.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenDark
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.Orange
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.RotationViewModel
import kotlinx.coroutines.launch

@Composable
fun RotationScreen(
    onBack: () -> Unit,
    onNavigateToAddReceipt: () -> Unit,
    viewModel: RotationViewModel = viewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val hasReordered by viewModel.hasReordered.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val isMyTurn = viewModel.isMyTurn

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

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
        TopBarWithBack(
            title = "Ordre de passage",
            onBack = onBack,
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }

            entries.isEmpty() -> {
                EmptyRotationContent(
                    isAdmin = isAdmin,
                    isSaving = isSaving,
                    onGenerate = { viewModel.generate() },
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 24.dp),
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Header card with green gradient
                    val activeEntries = entries.filter { it.isDisabled != true }
                    val currentPerson = activeEntries.firstOrNull()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GreenPrimary, GreenDark)
                                )
                            )
                            .padding(20.dp),
                    ) {
                        Column {
                            Text(
                                text = "Prochain aux courses",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                            )

                            Spacer(Modifier.height(12.dp))

                            if (currentPerson != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RoommateAvatar(
                                        colorHex = currentPerson.user.colorHex ?: "#FFFFFF",
                                        initial = currentPerson.user.initial
                                            ?: currentPerson.user.name.take(1).uppercase(),
                                        size = 48.dp,
                                        cornerRadius = 16.dp,
                                        fontSize = 18.sp,
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        text = currentPerson.user.name,
                                        fontFamily = FredokaFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = Color.White,
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Le tour avance apres l'ajout d'un ticket",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                            )

                            // Add ticket button if it's my turn
                            if (isMyTurn) {
                                Spacer(Modifier.height(14.dp))

                                Button(
                                    onClick = onNavigateToAddReceipt,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Ajouter un ticket",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Entries list
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground),
                    ) {
                        Column {
                            entries.forEachIndexed { index, entry ->
                                val isCurrentUser = entry.user.id == currentUserId
                                val isDisabled = entry.isDisabled == true
                                val isFirst = index == 0 && !isDisabled

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Index number
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isFirst) GreenPrimary.copy(alpha = 0.12f)
                                                else DividerColor
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontFamily = FredokaFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isFirst) GreenPrimary else SubtitleText,
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    // Avatar
                                    RoommateAvatar(
                                        colorHex = entry.user.colorHex ?: "#888888",
                                        initial = entry.user.initial
                                            ?: entry.user.name.take(1).uppercase(),
                                        size = 36.dp,
                                        cornerRadius = 12.dp,
                                        fontSize = 14.sp,
                                    )

                                    Spacer(Modifier.width(10.dp))

                                    // Name + "moi" badge
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = entry.user.name,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = if (isDisabled)
                                                    LightText else DarkText,
                                            )
                                            if (isCurrentUser) {
                                                Spacer(Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            GreenPrimary.copy(alpha = 0.12f)
                                                        )
                                                        .padding(
                                                            horizontal = 6.dp,
                                                            vertical = 2.dp
                                                        ),
                                                ) {
                                                    Text(
                                                        text = "moi",
                                                        fontFamily = DmSansFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp,
                                                        color = GreenPrimary,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Admin controls
                                    if (isAdmin) {
                                        // Activate/deactivate
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleMemberActive(entry.user.id)
                                            },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (isDisabled)
                                                    Icons.Default.PersonOutline
                                                else Icons.Default.PersonOff,
                                                contentDescription = if (isDisabled)
                                                    "Activer" else "Desactiver",
                                                tint = if (isDisabled) GreenPrimary
                                                else Orange,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }

                                        // Up chevron
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    viewModel.moveEntry(index, index - 1)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp),
                                            enabled = index > 0,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Monter",
                                                tint = if (index > 0) SubtitleText
                                                else DividerColor,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }

                                        // Down chevron
                                        IconButton(
                                            onClick = {
                                                if (index < entries.lastIndex) {
                                                    viewModel.moveEntry(index, index + 1)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp),
                                            enabled = index < entries.lastIndex,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Descendre",
                                                tint = if (index < entries.lastIndex)
                                                    SubtitleText else DividerColor,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }

                                    // Status badge
                                    if (!isAdmin) {
                                        val statusInfo = entryStatusInfo(
                                            isFirst = isFirst,
                                            isDisabled = isDisabled,
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(statusInfo.color.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                        ) {
                                            Text(
                                                text = statusInfo.label,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = statusInfo.color,
                                            )
                                        }
                                    }
                                }

                                if (index < entries.lastIndex) {
                                    HorizontalDivider(
                                        color = DividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Save button when admin has reordered
                    if (isAdmin && hasReordered) {
                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.saveOrder() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                            ),
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = "Sauvegarder l'ordre",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRotationContent(
    isAdmin: Boolean,
    isSaving: Boolean,
    onGenerate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.SwapVert,
            contentDescription = null,
            tint = LightText,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Aucun ordre de passage",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(8.dp))

        if (isAdmin) {
            Text(
                text = "Générez un ordre de passage pour vos colocataires",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SubtitleText,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onGenerate,
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Générer l'ordre",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        } else {
            Text(
                text = "L'administrateur n'a pas encore généré l'ordre de passage",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SubtitleText,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

private data class StatusInfo(
    val label: String,
    val color: Color,
)

private fun entryStatusInfo(
    isFirst: Boolean,
    isDisabled: Boolean,
): StatusInfo = when {
    isDisabled -> StatusInfo("Absent", Orange)
    isFirst -> StatusInfo("En cours", GreenPrimary)
    else -> StatusInfo("A venir", LightText)
}

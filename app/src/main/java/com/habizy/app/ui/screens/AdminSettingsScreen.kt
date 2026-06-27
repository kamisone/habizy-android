package com.habizy.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.AdminSettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminSettingsScreen(
    onBack: () -> Unit,
    viewModel: AdminSettingsViewModel = viewModel(),
) {
    val colocation by viewModel.colocation.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val colocationName by viewModel.colocationName.collectAsStateWithLifecycle()
    val spendingGapThreshold by viewModel.spendingGapThreshold.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var copiedLabel by remember { mutableStateOf(false) }
    var pendingToggleMember by remember { mutableStateOf<com.habizy.app.data.model.ColocationMemberResponse?>(null) }

    // Error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.ERROR)
                viewModel.clearErrorMessage()
            }
        }
    }

    // Success snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.SUCCESS)
                viewModel.clearSuccessMessage()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Paramètres Admin", onBack = onBack)

        if (isLoading && colocation == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 32.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Invite code card ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Code d'invitation",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = colocation?.colocation?.inviteCode ?: "---",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = DarkText,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val code = colocation?.colocation?.inviteCode ?: return@Button
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Code invitation", code)
                                )
                                copiedLabel = true
                                scope.launch {
                                    delay(2000)
                                    copiedLabel = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary.copy(alpha = 0.12f),
                                contentColor = GreenPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (copiedLabel) "Copie !" else "Copier",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Settings form card ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Paramètres",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = DarkText,
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Nom de la colocation",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = colocationName,
                            onValueChange = { viewModel.setColocationName(it) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor,
                            ),
                            singleLine = true,
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Seuil d'alerte écart de dépenses (€)",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = spendingGapThreshold,
                            onValueChange = { viewModel.setSpendingGapThreshold(it) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderColor,
                            ),
                            singleLine = true,
                        )

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.saveSettings() },
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
                                    color = CardBackground,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = "Sauvegarder",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Actions section ─────────────────────────────────
                Text(
                    text = "Actions",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 10.dp),
                )

                Button(
                    onClick = { viewModel.generateRotation() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue.copy(alpha = 0.12f),
                        contentColor = Blue,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Générer la rotation",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }

                // ── Admin management card ───────────────────────────
                val otherMembers = members.filter { it.user.id != currentUserId }
                if (otherMembers.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Gestion des admins",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = DarkText,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .padding(vertical = 8.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            otherMembers.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(GreenPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = member.user.initial ?: member.user.name.take(1).uppercase(),
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = GreenPrimary,
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.user.name,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = DarkText,
                                        )
                                        if (member.user.isAdmin) {
                                            Text(
                                                text = "Admin",
                                                fontFamily = DmSansFamily,
                                                fontSize = 12.sp,
                                                color = GreenPrimary,
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { pendingToggleMember = member },
                                    ) {
                                        Icon(
                                            imageVector = if (member.user.isAdmin) Icons.Filled.Shield else Icons.Outlined.Shield,
                                            contentDescription = if (member.user.isAdmin) "Retirer admin" else "Rendre admin",
                                            tint = if (member.user.isAdmin) GreenPrimary else SubtitleText,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog
    pendingToggleMember?.let { member ->
        val willBeAdmin = !member.user.isAdmin
        AlertDialog(
            onDismissRequest = { pendingToggleMember = null },
            title = {
                Text(
                    text = if (willBeAdmin) "Promouvoir admin ?" else "Retirer les droits admin ?",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = if (willBeAdmin)
                        "${member.user.name} pourra gérer la colocation."
                    else
                        "${member.user.name} ne sera plus administrateur.",
                    fontFamily = DmSansFamily,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleAdmin(member.user.id)
                        pendingToggleMember = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                ) {
                    Text("Confirmer", fontFamily = DmSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingToggleMember = null }) {
                    Text("Annuler", fontFamily = DmSansFamily, color = SubtitleText)
                }
            },
        )
    }
}

package com.habizy.app.ui.screens

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── ViewModel ────────────────────────────────────────────────────────

class CreateUserViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val colocationRepository = ColocationRepository(api, tokenManager)

    private val _inviteCode = MutableStateFlow<String?>(null)
    val inviteCode: StateFlow<String?> = _inviteCode.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            colocationRepository.getMyColocation()
                .onSuccess { detail -> _inviteCode.value = detail.colocation.inviteCode }
            _isLoading.value = false
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────

@Composable
fun CreateUserScreen(
    onBack: () -> Unit,
    viewModel: CreateUserViewModel = viewModel(),
) {
    val inviteCode by viewModel.inviteCode.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Inviter un colocataire", onBack = onBack)

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Instruction card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground)
                        .padding(24.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Code d'invitation",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DarkText,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Partage ce code avec ton colocataire. Il l'utilisera pour créer son compte et rejoindre la colocation.",
                            fontFamily = DmSansFamily,
                            fontSize = 13.sp,
                            color = SubtitleText,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(28.dp))

                        // Code pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(DividerColor)
                                .padding(horizontal = 28.dp, vertical = 16.dp),
                        ) {
                            Text(
                                text = inviteCode ?: "—",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = DarkText,
                                letterSpacing = 6.sp,
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    val code = inviteCode ?: return@Button
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Code d'invitation", code))
                                    scope.launch {
                                        snackbarHost.showTyped("Code copié !", SnackbarType.SUCCESS)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                enabled = inviteCode != null,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DividerColor, contentColor = DarkText),
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Copier", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    val code = inviteCode ?: return@Button
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Rejoins ma colocation sur Habizy avec ce code : $code")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Partager le code"))
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                enabled = inviteCode != null,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Partager", fontFamily = DmSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Le colocataire ouvrira l'application, choisira « Rejoindre une colocation » et entrera ce code avec ses informations personnelles.",
                    fontFamily = DmSansFamily,
                    fontSize = 13.sp,
                    color = SubtitleText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

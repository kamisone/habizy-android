package com.habizy.app.ui.screens

import android.app.Application
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ColocationMemberResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.habizy.app.util.userMessage

// ── ViewModel ────────────────────────────────────────────────────────

class MembersViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val authRepository = AuthRepository(api, tokenManager)

    private val _members = MutableStateFlow<List<ColocationMemberResponse>>(emptyList())
    val members: StateFlow<List<ColocationMemberResponse>> = _members.asStateFlow()

    private val _colocationId = MutableStateFlow<String?>(null)
    val colocationId: StateFlow<String?> = _colocationId.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val meDeferred = async { authRepository.getMe() }
            val colocationDeferred = async { colocationRepository.getMyColocation() }

            val me = meDeferred.await().getOrNull()
            if (me != null) {
                _currentUserId.value = me.id
                _isAdmin.value = me.isAdmin
            }

            colocationDeferred.await()
                .onSuccess { detail ->
                    _colocationId.value = detail.colocation.id
                    _members.value = detail.members
                }
                .onFailure { e ->
                    _errorMessage.value = e.userMessage()
                }

            _isLoading.value = false
        }
    }

    fun removeMember(userId: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val colId = _colocationId.value
            if (colId == null) {
                _errorMessage.value = "Aucune colocation"
                return@launch
            }

            val result = colocationRepository.removeMember(colId, userId)
            result.onSuccess {
                _members.value = _members.value.filter { it.user.id != userId }
                _successMessage.value = "Membre retire"
            }.onFailure { e ->
                _errorMessage.value = e.userMessage()
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

// ── Screen ───────────────────────────────────────────────────────────

@Composable
fun MembersScreen(
    onBack: () -> Unit,
    viewModel: MembersViewModel = viewModel(),
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    var memberToRemove by remember { mutableStateOf<String?>(null) }

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
        TopBarWithBack(title = "Colocataires", onBack = onBack)

        if (isLoading && members.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
            ) {
                itemsIndexed(members, key = { _, m -> m.id }) { index, member ->
                    MemberRow(
                        member = member,
                        isCurrentUser = member.user.id == currentUserId,
                        showRemoveButton = isAdmin
                                && member.user.id != currentUserId
                                && member.role != "admin",
                        onRemove = { memberToRemove = member.user.id },
                    )

                    if (index < members.lastIndex) {
                        HorizontalDivider(
                            color = DividerColor,
                            modifier = Modifier.padding(start = 80.dp),
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    // Remove confirmation dialog
    if (memberToRemove != null) {
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = {
                Text(
                    text = "Retirer ce membre ?",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                )
            },
            text = {
                Text(
                    text = "Cette action est irreversible.",
                    fontFamily = DmSansFamily,
                    color = SubtitleText,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    memberToRemove?.let { viewModel.removeMember(it) }
                    memberToRemove = null
                }) {
                    Text(
                        text = "Retirer",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralRed,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text(
                        text = "Annuler",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        color = SubtitleText,
                    )
                }
            },
            containerColor = androidx.compose.ui.graphics.Color.White,
            shape = RoundedCornerShape(22.dp),
        )
    }
}

@Composable
private fun MemberRow(
    member: ColocationMemberResponse,
    isCurrentUser: Boolean,
    showRemoveButton: Boolean,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoommateAvatar(
            colorHex = member.user.colorHex ?: "#888888",
            initial = member.user.initial ?: member.user.name.take(1).uppercase(),
            size = 48.dp,
            cornerRadius = 16.dp,
            fontSize = 18.sp,
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.user.name,
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = DarkText,
                )

                if (isCurrentUser) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "moi",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = GreenPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (member.role == "admin") "Administrateur" else "Colocataire",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = SubtitleText,
            )
        }

        if (showRemoveButton) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle,
                    contentDescription = "Retirer",
                    tint = CoralRed,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

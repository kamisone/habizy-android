package com.habizy.app.ui.screens

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.CreateUserResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.PresetHexColors
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.theme.toComposeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.habizy.app.util.userMessage

// ── ViewModel ────────────────────────────────────────────────────────

class CreateUserViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val colocationRepository = ColocationRepository(api, tokenManager)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _selectedColorHex = MutableStateFlow(PresetHexColors.first())
    val selectedColorHex: StateFlow<String> = _selectedColorHex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _createdUser = MutableStateFlow<CreateUserResponse?>(null)
    val createdUser: StateFlow<CreateUserResponse?> = _createdUser.asStateFlow()

    val canSubmit: Boolean
        get() = _name.value.isNotBlank() && _email.value.isNotBlank()

    fun setName(value: String) {
        _name.value = value
    }

    fun setEmail(value: String) {
        _email.value = value
    }

    fun setPassword(value: String) {
        _password.value = value
    }

    fun setSelectedColorHex(hex: String) {
        _selectedColorHex.value = hex
    }

    fun createUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                _isLoading.value = false
                return@launch
            }

            val result = colocationRepository.addMember(
                colocationId = colocationId,
                name = _name.value.trim(),
                email = _email.value.trim(),
                password = _password.value.ifBlank { null },
                colorHex = _selectedColorHex.value,
            )

            result.onSuccess { response ->
                _createdUser.value = response
            }.onFailure { e ->
                _errorMessage.value = e.userMessage()
            }

            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

// ── Screen ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(
    onBack: () -> Unit,
    viewModel: CreateUserViewModel = viewModel(),
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val selectedColorHex by viewModel.selectedColorHex.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val createdUser by viewModel.createdUser.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Error snackbar
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
        TopBarWithBack(title = "Nouveau colocataire", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ajouter un colocataire",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = DarkText,
            )

            Spacer(Modifier.height(16.dp))

            // ── Form card ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(CardBackground)
                    .padding(20.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.setName(it) },
                        label = {
                            Text(text = "Nom", fontFamily = DmSansFamily)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BorderColor,
                        ),
                        singleLine = true,
                    )

                    Spacer(Modifier.height(14.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.setEmail(it) },
                        label = {
                            Text(text = "Email", fontFamily = DmSansFamily)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BorderColor,
                        ),
                        singleLine = true,
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.setPassword(it) },
                        label = {
                            Text(
                                text = "Mot de passe (optionnel -- genere auto)",
                                fontFamily = DmSansFamily,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BorderColor,
                        ),
                        singleLine = true,
                    )

                    Spacer(Modifier.height(18.dp))

                    // Color picker
                    Text(
                        text = "Couleur",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = SubtitleText,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PresetHexColors.forEach { hex ->
                            val color = try {
                                hex.toComposeColor()
                            } catch (_: Exception) {
                                Color.Gray
                            }
                            val isSelected = hex == selectedColorHex

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, color, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clip(CircleShape)
                                    .clickable { viewModel.setSelectedColorHex(hex) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 28.dp else 40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(3.dp, Color.White, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                )
                            }
                        }
                    }

                    // Error text
                    val currentError = errorMessage
                    if (currentError != null) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = currentError,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = CoralRed,
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Submit button
                    Button(
                        onClick = { viewModel.createUser() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = viewModel.canSubmit && !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                        ),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CardBackground,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = "Creer le compte",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }

    // ── Success bottom sheet ────────────────────────────────────────
    if (createdUser != null) {
        val user = createdUser!!

        ModalBottomSheet(
            onDismissRequest = { onBack() },
            sheetState = sheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RoommateAvatar(
                    colorHex = user.user.colorHex ?: selectedColorHex,
                    initial = user.user.initial ?: user.user.name.take(1).uppercase(),
                    size = 72.dp,
                    cornerRadius = 24.dp,
                    fontSize = 26.sp,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = user.user.name,
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkText,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = user.user.email,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = SubtitleText,
                )

                // Generated password section
                if (user.generatedPassword != null) {
                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Mot de passe genere",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = SubtitleText,
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = user.generatedPassword!!,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DarkText,
                        )

                        Spacer(Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenPrimary.copy(alpha = 0.12f))
                                .clickable {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText(
                                            "Mot de passe",
                                            user.generatedPassword
                                        )
                                    )
                                    scope.launch {
                                        snackbarHost.showTyped(
                                            "Mot de passe copie",
                                            SnackbarType.SUCCESS
                                        )
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copier",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Copier",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = GreenPrimary,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Transmets ce mot de passe au colocataire pour qu'il puisse se connecter.",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = LightText,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { onBack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                    ),
                ) {
                    Text(
                        text = "Fermer",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

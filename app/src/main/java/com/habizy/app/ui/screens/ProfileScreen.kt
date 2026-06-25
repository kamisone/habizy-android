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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.ShimmerProfileLoading
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToMembers: () -> Unit,
    onNavigateToCreateUser: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val colocation by viewModel.colocation.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val isAdmin = viewModel.isAdmin
    val myTotalSpent = viewModel.myTotalSpent
    val ticketCount = viewModel.ticketCount

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

    var showPasswordSheet by remember { mutableStateOf(false) }
    var showNameSheet by remember { mutableStateOf(false) }
    val passwordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nameSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        if (isLoading && user == null) {
            ShimmerProfileLoading(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
            ) {
                // Avatar section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoommateAvatar(
                        colorHex = user?.colorHex ?: "#888888",
                        initial = user?.initial ?: user?.name?.take(1)?.uppercase() ?: "?",
                        size = 80.dp,
                        cornerRadius = 26.dp,
                        fontSize = 28.sp,
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.name ?: "",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = DarkText,
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier le nom",
                                tint = SubtitleText,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { showNameSheet = true },
                            )
                        }

                        if (isAdmin) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GreenPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Admin",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = GreenPrimary,
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user?.email ?: "",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // My total spent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .padding(16.dp),
                    ) {
                        Column {
                            Text(
                                text = "Mes dépenses",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = SubtitleText,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = String.format("%.2f €", myTotalSpent).replace(".", ","),
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = DarkText,
                            )
                        }
                    }

                    // Ticket count
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .padding(16.dp),
                    ) {
                        Column {
                            Text(
                                text = "Mes tickets",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = SubtitleText,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "$ticketCount",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = DarkText,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Members preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground)
                        .padding(16.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Colocataires",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = DarkText,
                            )
                            Text(
                                text = "Voir tous",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = GreenPrimary,
                                modifier = Modifier.clickable { onNavigateToMembers() },
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Overlapping avatars
                        val displayMembers = members.take(4)
                        val remaining = members.size - displayMembers.size

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            displayMembers.forEachIndexed { index, member ->
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-8 * index).dp)
                                        .zIndex((displayMembers.size - index).toFloat()),
                                ) {
                                    RoommateAvatar(
                                        colorHex = member.user.colorHex ?: "#888888",
                                        initial = member.user.initial
                                            ?: member.user.name.take(1).uppercase(),
                                        size = 40.dp,
                                        cornerRadius = 13.dp,
                                        fontSize = 15.sp,
                                    )
                                }
                            }
                            if (remaining > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-8 * displayMembers.size).dp)
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(BorderColor),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "+$remaining",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = SubtitleText,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Settings section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground),
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Key,
                            label = "Changer le mot de passe",
                            onClick = { showPasswordSheet = true },
                        )
                    }
                }

                // Admin section
                if (isAdmin) {
                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Administration",
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
                            .background(CardBackground),
                    ) {
                        Column {
                            SettingsRow(
                                icon = Icons.Default.PersonAdd,
                                label = "Ajouter un colocataire",
                                onClick = { onNavigateToCreateUser() },
                            )
                            HorizontalDivider(
                                color = DividerColor,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            SettingsRow(
                                icon = Icons.Default.Settings,
                                label = "Paramètres admin",
                                onClick = { onNavigateToAdmin() },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Logout button
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralRed.copy(alpha = 0.12f),
                        contentColor = CoralRed,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Se deconnecter",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }

    // Change password bottom sheet
    if (showPasswordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPasswordSheet = false },
            sheetState = passwordSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            ChangePasswordSheet(
                onSubmit = { current, new ->
                    viewModel.changePassword(current, new)
                    showPasswordSheet = false
                },
            )
        }
    }

    // Change name bottom sheet
    if (showNameSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNameSheet = false },
            sheetState = nameSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            ChangeNameSheet(
                currentName = user?.name ?: "",
                onSubmit = { name ->
                    viewModel.updateName(name)
                    showNameSheet = false
                },
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SubtitleText,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DarkText,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LightText,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ChangePasswordSheet(
    onSubmit: (currentPassword: String, newPassword: String) -> Unit,
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Changer le mot de passe",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = {
                Text(
                    text = "Mot de passe actuel",
                    fontFamily = DmSansFamily,
                )
            },
            visualTransformation = if (showCurrent) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showCurrent = !showCurrent }) {
                    Icon(
                        imageVector = if (showCurrent) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = SubtitleText,
                    )
                }
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

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = {
                Text(
                    text = "Nouveau mot de passe",
                    fontFamily = DmSansFamily,
                )
            },
            visualTransformation = if (showNew) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showNew = !showNew }) {
                    Icon(
                        imageVector = if (showNew) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = SubtitleText,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
            ),
            singleLine = true,
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSubmit(currentPassword, newPassword) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentPassword.isNotBlank() && newPassword.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
            ),
        ) {
            Text(
                text = "Modifier",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun ChangeNameSheet(
    currentName: String,
    onSubmit: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Modifier le nom",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(
                    text = "Nom",
                    fontFamily = DmSansFamily,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
            ),
            singleLine = true,
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSubmit(name) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && name != currentName,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
            ),
        ) {
            Text(
                text = "Enregistrer",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

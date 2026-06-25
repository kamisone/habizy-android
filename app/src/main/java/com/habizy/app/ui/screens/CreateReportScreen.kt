package com.habizy.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.CreateReportRequest
import com.habizy.app.data.model.ReportTagResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ReportRepository
import com.habizy.app.ui.components.LocalSnackbarHost
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
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.theme.tagColor
import com.habizy.app.data.repository.StorageRepository
import com.habizy.app.ui.theme.toComposeColor
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import com.habizy.app.util.userMessage

@Composable
fun CreateReportScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
) {
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    val api = ApiClient.apiService
    val reportRepository = remember { ReportRepository(api) }
    val storageRepository = remember { StorageRepository(api) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val photos = remember { mutableStateListOf<Bitmap>() }
    val selectedTags = remember { mutableStateListOf<String>() }
    var availableTags by remember { mutableStateOf<List<ReportTagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            photos.add(bitmap)
        }
    }

    // Context for TokenManager
    val context = androidx.compose.ui.platform.LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Load available tags
    LaunchedEffect(Unit) {
        val tokenManager = TokenManager(context)
        val colocationId = tokenManager.getColocationId() ?: return@LaunchedEffect
        reportRepository.getTags(colocationId)
            .onSuccess { availableTags = it }
    }

    // Shared submit action
    val submitReport: () -> Unit = {
        if (title.isNotBlank() && !isLoading) {
            scope.launch {
                isLoading = true
                errorText = null

                val tokenManager = TokenManager(context)
                val colocationId = tokenManager.getColocationId()
                if (colocationId == null) {
                    errorText = "Aucune colocation"
                    isLoading = false
                    return@launch
                }

                val uploadedUrls = mutableListOf<String>()
                for (bitmap in photos) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    storageRepository.uploadImage(stream.toByteArray(), "reports")
                        .onSuccess { uploadedUrls.add(it) }
                        .onFailure {
                            errorText = "Echec upload photo"
                            isLoading = false
                            return@launch
                        }
                }

                val body = CreateReportRequest(
                    colocationId = colocationId,
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    tags = selectedTags.toList().ifEmpty { null },
                    photoUrls = uploadedUrls,
                )

                val result = reportRepository.create(body)
                result.onSuccess {
                    scope.launch {
                        snackbarHost.showTyped(
                            "Signalement cree",
                            SnackbarType.SUCCESS,
                        )
                    }
                    onCreated()
                    onBack()
                }.onFailure { e ->
                    errorText = e.userMessage()
                }

                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        // Top bar with back + send button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopBarWithBack(
                title = "Nouveau signalement",
                onBack = onBack,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = submitReport,
                enabled = title.isNotBlank() && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = GreenPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Envoyer",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (title.isNotBlank()) GreenPrimary else LightText,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
        ) {
            // Photos section
            Text(
                text = "Photos",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = DarkText,
            )
            Spacer(Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                photos.forEachIndexed { index, bitmap ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Photo ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(CoralRed)
                                .clickable { photos.removeAt(index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Supprimer",
                                tint = CardBackground,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                // Camera add button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderColor.copy(alpha = 0.5f))
                        .clickable { launchCamera() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Prendre une photo",
                        tint = SubtitleText,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        text = "Titre",
                        fontFamily = DmSansFamily,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary,
                ),
                singleLine = true,
            )

            Spacer(Modifier.height(14.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = {
                    Text(
                        text = "Description",
                        fontFamily = DmSansFamily,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary,
                ),
            )

            Spacer(Modifier.height(20.dp))

            // Tags section
            if (availableTags.isNotEmpty()) {
                Text(
                    text = "Tags",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = DarkText,
                )
                Spacer(Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag.title)
                        val color = try {
                            tag.color.toComposeColor()
                        } catch (_: Exception) {
                            tagColor(tag.title)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(color)
                                    } else {
                                        Modifier.background(color.copy(alpha = 0.14f))
                                    }
                                )
                                .clickable {
                                    if (isSelected) {
                                        selectedTags.remove(tag.title)
                                    } else {
                                        selectedTags.add(tag.title)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = tag.title,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) CardBackground else color,
                            )
                        }
                    }
                }
            }

            // Error text
            if (errorText != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = errorText!!,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = CoralRed,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = submitReport,
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = CardBackground,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = CardBackground,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = "Envoyer",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

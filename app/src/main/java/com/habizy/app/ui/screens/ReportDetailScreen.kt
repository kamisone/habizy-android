package com.habizy.app.ui.screens

import android.app.Application
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ReportDetailResponse
import com.habizy.app.data.model.ReportTagResponse
import com.habizy.app.data.model.UpdateReportRequest
import com.habizy.app.data.model.UserResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ReportRepository
import com.habizy.app.ui.components.ImageCarousel
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.BodyText
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
import com.habizy.app.ui.theme.tagColor
import com.habizy.app.ui.theme.toComposeColor
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// ReportDetailViewModel (inline, scoped to this file)
// ---------------------------------------------------------------------------

class ReportDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val reportRepository = ReportRepository(api)

    private val _detail = MutableStateFlow<ReportDetailResponse?>(null)
    val detail: StateFlow<ReportDetailResponse?> = _detail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _currentUser = MutableStateFlow<UserResponse?>(null)
    val currentUser: StateFlow<UserResponse?> = _currentUser.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _availableTags = MutableStateFlow<List<ReportTagResponse>>(emptyList())
    val availableTags: StateFlow<List<ReportTagResponse>> = _availableTags.asStateFlow()

    val canEdit: Boolean
        get() {
            val user = _currentUser.value ?: return false
            val report = _detail.value ?: return false
            return user.id == report.user.id || user.isAdmin
        }

    fun load(reportId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val meDeferred = async {
                try {
                    api.getMe()
                } catch (_: Exception) {
                    null
                }
            }
            val detailDeferred = async { reportRepository.getDetail(reportId) }

            _currentUser.value = meDeferred.await()

            detailDeferred.await()
                .onSuccess { _detail.value = it }
                .onFailure { e -> _errorMessage.value = e.message ?: "Erreur chargement" }

            // Load available tags for edit
            val colocationId = tokenManager.getColocationId()
            if (colocationId != null) {
                reportRepository.getTags(colocationId)
                    .onSuccess { _availableTags.value = it }
            }

            _isLoading.value = false
        }
    }

    fun addComment(reportId: String, content: String) {
        viewModelScope.launch {
            val result = reportRepository.addComment(reportId, content)
            result.onSuccess { newComment ->
                val current = _detail.value
                if (current != null) {
                    _detail.value = current.copy(comments = current.comments + newComment)
                }
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur ajout commentaire"
            }
        }
    }

    fun updateReport(reportId: String, title: String, description: String, tags: List<String>?) {
        viewModelScope.launch {
            val body = UpdateReportRequest(
                title = title,
                description = description,
                tags = tags,
            )
            val result = reportRepository.update(reportId, body)
            result.onSuccess { updated ->
                _detail.value = updated
                _successMessage.value = "Signalement mis a jour"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur mise a jour"
            }
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            val result = reportRepository.delete(reportId)
            result.onSuccess {
                _deleted.value = true
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur suppression"
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

// ---------------------------------------------------------------------------
// Screen composable
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBack: () -> Unit,
) {
    val viewModel: ReportDetailViewModel = viewModel()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    var commentText by remember { mutableStateOf("") }
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Initial load
    LaunchedEffect(reportId) {
        viewModel.load(reportId)
    }

    // On deleted, navigate back
    LaunchedEffect(deleted) {
        if (deleted) {
            onBack()
        }
    }

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
        TopBarWithBack(title = "Signalement", onBack = onBack)

        if (isLoading && detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                )
            }
        } else if (detail != null) {
            val report = detail!!

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 8.dp),
            ) {
                // Photo carousel
                if (!report.photoUrls.isNullOrEmpty()) {
                    ImageCarousel(
                        imageUrls = report.photoUrls,
                        height = 220.dp,
                        cornerRadius = 18.dp,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Info card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                ) {
                    Column {
                        // Tags
                        if (!report.tags.isNullOrEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                report.tags.forEach { tag ->
                                    val chipColor = tagColor(tag)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(chipColor.copy(alpha = 0.14f))
                                            .padding(horizontal = 10.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text = tag,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = chipColor,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // Title
                        Text(
                            text = report.title,
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DarkText,
                        )

                        // Description
                        if (!report.description.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = report.description,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = BodyText,
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Author + time + actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RoommateAvatar(
                                colorHex = report.user.colorHex ?: "#888888",
                                initial = report.user.initial
                                    ?: report.user.name.take(1).uppercase(),
                                size = 28.dp,
                                cornerRadius = 9.dp,
                                fontSize = 12.sp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = report.user.name,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = BodyText,
                            )

                            val timeAgo = formatTimeAgo(report.createdAt)
                            if (timeAgo.isNotBlank()) {
                                Text(
                                    text = " · $timeAgo",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = LightText,
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            if (viewModel.canEdit) {
                                IconButton(
                                    onClick = { showEditSheet = true },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifier",
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = CoralRed,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Comments card
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
                            text = "Commentaires (${report.comments.size})",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = DarkText,
                        )

                        if (report.comments.isEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Aucun commentaire pour le moment",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = SubtitleText,
                            )
                        } else {
                            Spacer(Modifier.height(12.dp))
                            report.comments.forEachIndexed { index, comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    RoommateAvatar(
                                        colorHex = comment.user.colorHex ?: "#888888",
                                        initial = comment.user.initial
                                            ?: comment.user.name.take(1).uppercase(),
                                        size = 30.dp,
                                        cornerRadius = 10.dp,
                                        fontSize = 12.sp,
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = comment.user.name,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = DarkText,
                                            )
                                            val commentTime = formatTimeAgo(comment.createdAt)
                                            if (commentTime.isNotBlank()) {
                                                Text(
                                                    text = " · $commentTime",
                                                    fontFamily = DmSansFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = LightText,
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = comment.content,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = BodyText,
                                        )
                                    }
                                }
                                if (index < report.comments.lastIndex) {
                                    HorizontalDivider(
                                        color = DividerColor,
                                        modifier = Modifier.padding(start = 40.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Bottom comment input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .imePadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = {
                        Text(
                            text = "Ajouter un commentaire...",
                            fontFamily = DmSansFamily,
                            fontSize = 14.sp,
                            color = LightText,
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ScreenBackground,
                        unfocusedContainerColor = ScreenBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = GreenPrimary,
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (commentText.isNotBlank()) GreenPrimary
                            else GreenPrimary.copy(alpha = 0.4f)
                        )
                        .clickable(enabled = commentText.isNotBlank()) {
                            val text = commentText.trim()
                            if (text.isNotBlank()) {
                                viewModel.addComment(reportId, text)
                                commentText = ""
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Envoyer",
                        tint = CardBackground,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }

    // Edit bottom sheet
    if (showEditSheet && detail != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = editSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            EditReportSheet(
                report = detail!!,
                availableTags = availableTags,
                onSave = { title, description, tags ->
                    viewModel.updateReport(reportId, title, description, tags)
                    showEditSheet = false
                },
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Supprimer le signalement",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = DarkText,
                )
            },
            text = {
                Text(
                    text = "Cette action est irreversible.",
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = BodyText,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReport(reportId)
                    },
                ) {
                    Text(
                        text = "Supprimer",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralRed,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Annuler",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        color = SubtitleText,
                    )
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(22.dp),
        )
    }
}

@Composable
private fun EditReportSheet(
    report: ReportDetailResponse,
    availableTags: List<ReportTagResponse>,
    onSave: (title: String, description: String, tags: List<String>?) -> Unit,
) {
    var editTitle by remember { mutableStateOf(report.title) }
    var editDescription by remember { mutableStateOf(report.description ?: "") }
    val selectedTags = remember {
        mutableStateListOf<String>().apply {
            addAll(report.tags ?: emptyList())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Modifier le signalement",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
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

        OutlinedTextField(
            value = editDescription,
            onValueChange = { editDescription = it },
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

        Spacer(Modifier.height(14.dp))

        // Tag selector
        if (availableTags.isNotEmpty()) {
            Text(
                text = "Tags",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = DarkText,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
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
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = tag.title,
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) CardBackground else color,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (editTitle.isNotBlank()) {
                    onSave(
                        editTitle.trim(),
                        editDescription.trim(),
                        selectedTags.toList().ifEmpty { null },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = editTitle.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = CardBackground,
            ),
        ) {
            Text(
                text = "Enregistrer",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

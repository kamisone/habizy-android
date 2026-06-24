package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.habizy.app.data.model.ReportResponse
import com.habizy.app.data.model.ReportTagResponse
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.components.ShimmerReportsLoading
import com.habizy.app.ui.components.SnackbarType
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
import com.habizy.app.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Parse an ISO date string and return a French relative time string.
 */
fun formatTimeAgo(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val date = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        val now = ZonedDateTime.now()
        val duration = Duration.between(date, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        when {
            minutes < 1 -> "a l'instant"
            minutes < 60 -> "il y a ${minutes}min"
            hours < 24 -> "il y a ${hours}h"
            days < 7 -> "il y a ${days}j"
            days < 30 -> "il y a ${days / 7} sem"
            days < 365 -> "il y a ${days / 30} mois"
            else -> "il y a ${days / 365} an${if (days / 365 > 1) "s" else ""}"
        }
    } catch (_: Exception) {
        ""
    }
}

private val PresetTagHexColors = listOf(
    "#FF6B5E", "#FFB020", "#3B82F6", "#7C6BFF",
    "#17A877", "#EC4899", "#14B8A6", "#F97316"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToReportDetail: (String) -> Unit,
    onNavigateToCreateReport: () -> Unit,
    viewModel: ReportsViewModel = viewModel(),
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val tagFilter by viewModel.tagFilter.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    var showAddTagSheet by remember { mutableStateOf(false) }
    val addTagSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.ERROR)
                viewModel.clearErrorMessage()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        if (isLoading && reports.isEmpty()) {
            ShimmerReportsLoading(modifier = Modifier.fillMaxSize())
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
                    text = "Signalements",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DarkText,
                )

                Spacer(Modifier.height(16.dp))

                // Report button
                Button(
                    onClick = onNavigateToCreateReport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralRed,
                        contentColor = CardBackground,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Signaler un probleme",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Tag filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // "Tous" chip
                    TagFilterChip(
                        label = "Tous",
                        color = GreenPrimary,
                        isSelected = tagFilter == null,
                        onClick = { viewModel.setTagFilter(null) },
                    )
                    tags.forEach { tag ->
                        val color = try {
                            tag.color.toComposeColor()
                        } catch (_: Exception) {
                            tagColor(tag.title)
                        }
                        TagFilterChip(
                            label = tag.title,
                            color = color,
                            isSelected = tagFilter == tag.title,
                            onClick = { viewModel.setTagFilter(tag.title) },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Reports list card
                if (reports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Aucun signalement",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = SubtitleText,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground),
                    ) {
                        Column {
                            reports.forEachIndexed { index, report ->
                                ReportRow(
                                    report = report,
                                    onClick = { onNavigateToReportDetail(report.id) },
                                )
                                if (index < reports.lastIndex) {
                                    HorizontalDivider(
                                        color = DividerColor,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // Admin tag management
                if (isAdmin) {
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Gerer les tags",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = DarkText,
                        )
                        IconButton(onClick = { showAddTagSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter un tag",
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (tags.isEmpty()) {
                        Text(
                            text = "Aucun tag",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardBackground),
                        ) {
                            Column {
                                tags.forEachIndexed { index, tag ->
                                    TagManagementRow(
                                        tag = tag,
                                        onDelete = { viewModel.deleteTag(tag.id) },
                                    )
                                    if (index < tags.lastIndex) {
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
    }

    // Add tag bottom sheet
    if (showAddTagSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTagSheet = false },
            sheetState = addTagSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            AddTagSheet(
                onAdd = { title, color ->
                    viewModel.createTag(title, color)
                    showAddTagSheet = false
                },
            )
        }
    }
}

@Composable
private fun TagFilterChip(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isSelected) {
                    Modifier.background(color)
                } else {
                    Modifier.border(1.dp, color, RoundedCornerShape(20.dp))
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = if (isSelected) CardBackground else color,
        )
    }
}

@Composable
private fun ReportRow(
    report: ReportResponse,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Photo thumbnail or flag placeholder
        val firstPhoto = report.photoUrls?.firstOrNull()
        if (firstPhoto != null) {
            AsyncImage(
                model = firstPhoto,
                contentDescription = "Photo",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CoralRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = CoralRed,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
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
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = tag,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = chipColor,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Title
            Text(
                text = report.title,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Description
            if (!report.description.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = report.description,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = BodyText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Author + time ago + comment count
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoommateAvatar(
                    colorHex = report.user.colorHex ?: "#888888",
                    initial = report.user.initial ?: report.user.name.take(1).uppercase(),
                    size = 18.dp,
                    cornerRadius = 6.dp,
                    fontSize = 9.sp,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = report.user.name,
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = SubtitleText,
                )

                val timeAgo = formatTimeAgo(report.createdAt)
                if (timeAgo.isNotBlank()) {
                    Text(
                        text = " · $timeAgo",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = LightText,
                    )
                }

                val commentCount = report.commentCount ?: 0
                if (commentCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = LightText,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "$commentCount",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = LightText,
                    )
                }
            }
        }
    }
}

@Composable
private fun TagManagementRow(
    tag: ReportTagResponse,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = try {
            tag.color.toComposeColor()
        } catch (_: Exception) {
            SubtitleText
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = tag.title,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DarkText,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer",
                tint = CoralRed,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AddTagSheet(
    onAdd: (title: String, color: String) -> Unit,
) {
    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PresetTagHexColors.first()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Nouveau tag",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = tagName,
            onValueChange = { tagName = it },
            label = {
                Text(
                    text = "Nom du tag",
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

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Couleur",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PresetTagHexColors.forEach { hex ->
                val color = try {
                    hex.toComposeColor()
                } catch (_: Exception) {
                    SubtitleText
                }
                val isSelected = selectedColor == hex
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) {
                                Modifier.border(3.dp, DarkText, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { selectedColor = hex },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (tagName.isNotBlank()) {
                    onAdd(tagName.trim(), selectedColor)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = tagName.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = CardBackground,
            ),
        ) {
            Text(
                text = "Ajouter",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

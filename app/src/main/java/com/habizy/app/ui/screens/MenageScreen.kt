package com.habizy.app.ui.screens

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.MarkMenageDoneRequest
import com.habizy.app.data.model.MenageBoardMember
import com.habizy.app.data.model.MenageWeekResponse
import com.habizy.app.data.model.UpdateTaskDescriptionRequest
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.ui.components.RoommateAvatar
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightCardBg
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.Purple
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import com.habizy.app.util.userMessage

// ── Inline ViewModel ──

class MenageViewModel(application: Application) : AndroidViewModel(application) {

    private val api = ApiClient.apiService
    private val tokenManager = TokenManager(application)

    private val _weekData = MutableStateFlow<MenageWeekResponse?>(null)
    val weekData: StateFlow<MenageWeekResponse?> = _weekData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        load()
    }

    fun load(isRefresh: Boolean = false, silent: Boolean = false) {
        if (silent && (_isLoading.value || _isRefreshing.value)) return
        viewModelScope.launch {
            if (!silent) {
                if (isRefresh) _isRefreshing.value = true else _isLoading.value = true
                _errorMessage.value = null
            }
            try {
                val me = api.getMe()
                _currentUserId.value = me.id
                _isAdmin.value = me.isAdmin

                val colocationId = tokenManager.getColocationId()
                    ?: throw Exception("Colocation introuvable")
                val week = api.getMenageWeek(colocationId)
                _weekData.value = week
            } catch (e: Exception) {
                if (!silent) _errorMessage.value = e.userMessage()
            } finally {
                if (!silent) {
                    if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
                }
            }
        }
    }

    fun refresh() = load(isRefresh = true)
    fun silentRefresh() = load(silent = true)

    fun markDone(comment: String?) {
        viewModelScope.launch {
            try {
                val colocationId = tokenManager.getColocationId()
                    ?: throw Exception("Colocation introuvable")
                api.markMenageDone(colocationId, MarkMenageDoneRequest(comment))
                load()
            } catch (e: Exception) {
                _errorMessage.value = e.userMessage()
            }
        }
    }

    fun undoDone() {
        viewModelScope.launch {
            try {
                val colocationId = tokenManager.getColocationId()
                    ?: throw Exception("Colocation introuvable")
                api.undoMenageDone(colocationId)
                load()
            } catch (e: Exception) {
                _errorMessage.value = e.userMessage()
            }
        }
    }

    fun updateTaskDescription(description: String) {
        viewModelScope.launch {
            try {
                val colocationId = tokenManager.getColocationId()
                    ?: throw Exception("Colocation introuvable")
                api.updateMenageTaskDescription(colocationId, UpdateTaskDescriptionRequest(description))
                load()
            } catch (e: Exception) {
                _errorMessage.value = e.userMessage()
            }
        }
    }
}

// ── Helper functions ──

/** Returns 0 (Mon) .. 6 (Sun) for a given ISO date string "yyyy-MM-dd". */
private fun getDayOfWeek(isoDate: String): Int {
    val date = LocalDate.parse(isoDate)
    return date.dayOfWeek.value - 1 // Monday=0 .. Sunday=6
}

/** Returns list of 7 day-of-month numbers starting from weekStart (ISO date). */
private fun getWeekDates(weekStart: String): List<Int> {
    val start = LocalDate.parse(weekStart)
    return (0..6).map { start.plusDays(it.toLong()).dayOfMonth }
}

/** Checks if weekStart + dayIndex (0=Mon) is today. */
private fun isToday(weekStart: String, dayIndex: Int): Boolean {
    val start = LocalDate.parse(weekStart)
    val date = start.plusDays(dayIndex.toLong())
    return date == LocalDate.now()
}

/** Format weekStart for display: "dd/MM". */
private fun formatWeekStart(weekStart: String): String {
    return try {
        val date = LocalDate.parse(weekStart)
        date.format(DateTimeFormatter.ofPattern("dd/MM"))
    } catch (_: Exception) {
        weekStart
    }
}

private val dayLabels = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

// ── Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenageScreen() {
    val viewModel: MenageViewModel = viewModel()
    val weekData by viewModel.weekData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()

    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showCommentSheet by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var showEditTaskSheet by remember { mutableStateOf(false) }
    var editTaskText by remember { mutableStateOf("") }

    // Derive state from weekData
    val data = weekData
    val board = data?.board ?: emptyList()
    val totalMembers = data?.totalMembers ?: 0
    val totalDone = data?.totalDone ?: 0
    val progress = if (totalMembers > 0) totalDone.toFloat() / totalMembers else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress",
    )

    // Current user board entry
    val myEntry = board.find { it.userId == currentUserId }
    val iDoneThisWeek = myEntry?.done == true

    // Today taken info
    val todayTakenBy = data?.todayTakenBy
    val todayMember = board.find { it.userId == todayTakenBy }
    val iDidItToday = todayTakenBy == currentUserId

    // Today comment from the person who did it today
    val todayComment = if (todayMember != null) {
        // Find the member whose doneAt is today
        val todayStr = LocalDate.now().toString()
        board.find { it.doneAt?.startsWith(todayStr) == true }?.comment
    } else null

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        if (isLoading && data == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
            ) {
                // ── Progress header card (purple gradient) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Purple, Color(0xFF5B4FCC)),
                            )
                        )
                        .padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left side
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ménage",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color.White,
                            )
                            if (data != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Semaine du ${formatWeekStart(data.weekStart)}",
                                    fontFamily = DmSansFamily,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$totalDone/$totalMembers ont fait le ménage",
                                    fontFamily = DmSansFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                )

                                // Task description
                                val taskDesc = data.taskDescription
                                if (!taskDesc.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = taskDesc,
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.75f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false),
                                        )
                                        if (isAdmin) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier la tâche",
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        editTaskText = taskDesc
                                                        showEditTaskSheet = true
                                                    },
                                            )
                                        }
                                    }
                                } else if (isAdmin) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            editTaskText = ""
                                            showEditTaskSheet = true
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Ajouter une tâche",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Ajouter une description",
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            }
                        }

                        // Right side - circular progress
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .drawBehind {
                                    val strokeWidth = 6.dp.toPx()
                                    val radius = (size.minDimension - strokeWidth) / 2
                                    val topLeft = Offset(
                                        (size.width - radius * 2) / 2,
                                        (size.height - radius * 2) / 2,
                                    )
                                    val arcSize = Size(radius * 2, radius * 2)

                                    // Background circle
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.2f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    )
                                    // Progress arc
                                    drawArc(
                                        color = Color.White,
                                        startAngle = -90f,
                                        sweepAngle = animatedProgress * 360f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Today highlight ──
                if (todayMember != null && todayTakenBy != currentUserId) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GreenPrimary.copy(alpha = 0.08f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${todayMember.name} a fait le ménage aujourd'hui",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = DarkText,
                            )
                            if (!todayComment.isNullOrBlank()) {
                                Text(
                                    text = todayComment,
                                    fontFamily = DmSansFamily,
                                    fontSize = 12.sp,
                                    color = SubtitleText,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Action button ──
                if (data != null) {
                    val hasNotDoneThisWeek = !iDoneThisWeek
                    val noOneTookToday = todayTakenBy == null
                    val canMarkDone = hasNotDoneThisWeek && noOneTookToday
                    val someonElseToday = todayTakenBy != null && todayTakenBy != currentUserId

                    when {
                        canMarkDone -> {
                            Button(
                                onClick = {
                                    commentText = ""
                                    showCommentSheet = true
                                },
                                shape = RoundedCornerShape(18.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimary,
                                    contentColor = Color.White,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                            ) {
                                Text(
                                    text = "J'ai fait le ménage !",
                                    fontFamily = FredokaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                )
                            }
                        }

                        iDidItToday -> {
                            Button(
                                onClick = { viewModel.undoDone() },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LightCardBg,
                                    contentColor = DarkText,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                            ) {
                                Text(
                                    text = "Annuler mon ménage",
                                    fontFamily = FredokaFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                )
                            }
                        }

                        iDoneThisWeek -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = LightCardBg,
                                    disabledContentColor = SubtitleText,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                            ) {
                                Text(
                                    text = "Ménage déjà fait cette semaine",
                                    fontFamily = FredokaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                )
                            }
                        }

                        someonElseToday -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = LightCardBg,
                                    disabledContentColor = SubtitleText,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                            ) {
                                Text(
                                    text = "Déjà pris aujourd'hui",
                                    fontFamily = FredokaFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Week calendar ──
                if (data != null) {
                    val weekDates = getWeekDates(data.weekStart)

                    // Build map of dayIndex -> member who did menage that day
                    val doneMemberByDay = mutableMapOf<Int, MenageBoardMember>()
                    for (member in board) {
                        if (member.done && member.doneAt != null) {
                            try {
                                val dayIdx = getDayOfWeek(member.doneAt.take(10))
                                doneMemberByDay[dayIdx] = member
                            } catch (_: Exception) {
                                // ignore parse errors
                            }
                        }
                    }

                    // Row 1: Mon-Thu
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (i in 0..3) {
                            DayCell(
                                dayLabel = dayLabels[i],
                                dateNumber = weekDates[i],
                                isToday = isToday(data.weekStart, i),
                                doneMember = doneMemberByDay[i],
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Row 2: Fri-Sun + spacer
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (i in 4..6) {
                            DayCell(
                                dayLabel = dayLabels[i],
                                dateNumber = weekDates[i],
                                isToday = isToday(data.weekStart, i),
                                doneMember = doneMemberByDay[i],
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                        // Spacer cell for alignment
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ── Participants card ──
                if (board.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground)
                            .padding(16.dp),
                    ) {
                        Column {
                            Text(
                                text = "Participants",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = DarkText,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            board.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RoommateAvatar(
                                        colorHex = member.colorHex ?: "#888888",
                                        initial = member.initial ?: member.name.take(1).uppercase(),
                                        size = 38.dp,
                                        cornerRadius = 13.dp,
                                        fontSize = 15.sp,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = member.name,
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = DarkText,
                                        modifier = Modifier.weight(1f),
                                    )
                                    // Badge
                                    if (member.done) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(GreenPrimary.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                        ) {
                                            Text(
                                                text = "Fait",
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = GreenPrimary,
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(LightCardBg)
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                        ) {
                                            Text(
                                                text = "En attente",
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = SubtitleText,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontFamily = DmSansFamily,
                        fontSize = 13.sp,
                        color = Color(0xFFFF6B5E),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    // ── Comment bottom sheet ──
    if (showCommentSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = sheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Ajouter un commentaire (optionnel)",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = DarkText,
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { if (it.length <= 50) commentText = it },
                    placeholder = {
                        Text(
                            text = "Ex: Passe l'aspirateur et la serpillière",
                            fontFamily = DmSansFamily,
                            color = SubtitleText,
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 15.sp,
                        color = DarkText,
                    ),
                    supportingText = {
                        Text(
                            text = "${commentText.length}/50",
                            fontFamily = DmSansFamily,
                            fontSize = 12.sp,
                            color = SubtitleText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = GreenPrimary,
                    ),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        viewModel.markDone(commentText.ifBlank { null })
                        showCommentSheet = false
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                ) {
                    Text(
                        text = "Valider",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }

    // ── Edit task bottom sheet (admin) ──
    if (showEditTaskSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEditTaskSheet = false },
            sheetState = sheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "Description de la tâche",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = DarkText,
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = editTaskText,
                    onValueChange = { editTaskText = it },
                    placeholder = {
                        Text(
                            text = "Decrivez la tâche de ménage...",
                            fontFamily = DmSansFamily,
                            color = SubtitleText,
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = DmSansFamily,
                        fontSize = 15.sp,
                        color = DarkText,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = GreenPrimary,
                    ),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        viewModel.updateTaskDescription(editTaskText.trim())
                        showEditTaskSheet = false
                    },
                    enabled = editTaskText.isNotBlank(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                ) {
                    Text(
                        text = "Enregistrer",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayLabel: String,
    dateNumber: Int,
    isToday: Boolean,
    doneMember: MenageBoardMember?,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        isToday -> GreenPrimary
        doneMember != null -> GreenPrimary.copy(alpha = 0.4f)
        else -> BorderColor
    }
    val bgColor = when {
        doneMember != null -> GreenPrimary.copy(alpha = 0.06f)
        else -> CardBackground
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp),
            )
            .background(bgColor)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = dayLabel,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isToday) GreenPrimary else SubtitleText,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isToday) GreenPrimary else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$dateNumber",
                fontFamily = FredokaFamily,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isToday) Color.White else DarkText,
            )
        }

        if (doneMember != null) {
            Spacer(modifier = Modifier.height(6.dp))
            RoommateAvatar(
                colorHex = doneMember.colorHex ?: "#888888",
                initial = doneMember.initial ?: doneMember.name.take(1).uppercase(),
                size = 28.dp,
                cornerRadius = 10.dp,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = doneMember.name.split(" ").first(),
                fontFamily = DmSansFamily,
                fontSize = 10.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!doneMember.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = doneMember.comment,
                    fontFamily = DmSansFamily,
                    fontSize = 9.sp,
                    color = SubtitleText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

package com.habizy.app.ui.screens

import android.app.Application
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.habizy.app.data.model.ExpenseStatsResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ReceiptRepository
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.theme.BodyText
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightCardBg
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.theme.toComposeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.habizy.app.util.userMessage

// -- Inline ViewModel --

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val receiptRepository = ReceiptRepository(ApiClient.apiService)

    private val _stats = MutableStateFlow<ExpenseStatsResponse?>(null)
    val stats: StateFlow<ExpenseStatsResponse?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                _isLoading.value = false
                return@launch
            }

            receiptRepository.getStats(colocationId)
                .onSuccess { _stats.value = it }
                .onFailure { e -> _errorMessage.value = e.userMessage() }

            _isLoading.value = false
        }
    }
}

// -- Chart colors --

private val chartColors = listOf(
    "#17A877", "#3B82F6", "#FFB020", "#FF6B5E",
    "#7C6BFF", "#14B8A6", "#F97316", "#EC4899",
)

// -- Screen --

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Statistiques", onBack = onBack)

        if (isLoading && stats == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (stats != null) {
            val expenseStats = stats!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // 1. Total card
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
                            text = "Total des depenses",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = String.format("%.2f €", expenseStats.totalSpent)
                                .replace(".", ","),
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = DarkText,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${expenseStats.byRoommate.size} colocataires · ${expenseStats.byCategory.size} categories",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = SubtitleText,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 2. Per-person card
                if (expenseStats.byRoommate.isNotEmpty()) {
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
                                text = "Depenses par colocataire",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = DarkText,
                            )

                            Spacer(Modifier.height(16.dp))

                            expenseStats.byRoommate.forEachIndexed { index, roommate ->
                                val barColor = chartColors[index % chartColors.size]
                                    .toComposeColor()
                                val name = roommate.user?.name ?: "Inconnu"
                                val amount = String.format("%.2f €", roommate.total)
                                    .replace(".", ",")
                                val percentage = "${(roommate.fraction * 100).toInt()}%"

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = name,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = BodyText,
                                        )
                                        Row {
                                            Text(
                                                text = amount,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = DarkText,
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = percentage,
                                                fontFamily = DmSansFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp,
                                                color = SubtitleText,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))

                                    // Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(LightCardBg),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(
                                                    roommate.fraction
                                                        .toFloat()
                                                        .coerceIn(0f, 1f)
                                                )
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(barColor),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                }

                // 3. Balance card
                if (expenseStats.byRoommate.size > 1) {
                    val average = expenseStats.totalSpent / expenseStats.byRoommate.size

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
                                text = "Equilibre des depenses",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = DarkText,
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Moyenne par personne: ${
                                    String.format("%.2f €", average).replace(".", ",")
                                }",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = SubtitleText,
                            )

                            Spacer(Modifier.height(14.dp))

                            expenseStats.byRoommate.forEachIndexed { index, roommate ->
                                val name = roommate.user?.name ?: "Inconnu"
                                val diff = roommate.total - average
                                val diffText = String.format("%+.2f €", diff).replace(".", ",")
                                val diffColor = if (diff >= 0) GreenPrimary else CoralRed

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = name,
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = BodyText,
                                    )
                                    Text(
                                        text = diffText,
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = diffColor,
                                    )
                                }

                                if (index < expenseStats.byRoommate.lastIndex) {
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                }

                // 4. Category card
                if (expenseStats.byCategory.isNotEmpty()) {
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
                                text = "Depenses par categorie",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = DarkText,
                            )

                            Spacer(Modifier.height(14.dp))

                            expenseStats.byCategory.forEachIndexed { index, cat ->
                                val dotColor = chartColors[index % chartColors.size]
                                    .toComposeColor()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Colored dot
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(dotColor),
                                    )

                                    Spacer(Modifier.width(10.dp))

                                    Text(
                                        text = cat.category,
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = BodyText,
                                        modifier = Modifier.weight(1f),
                                    )

                                    Text(
                                        text = String.format("%.2f €", cat.total)
                                            .replace(".", ","),
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = DarkText,
                                    )
                                }

                                if (index < expenseStats.byCategory.lastIndex) {
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = SubtitleText,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Erreur",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = SubtitleText,
                    )
                }
            }
        }
    }
}

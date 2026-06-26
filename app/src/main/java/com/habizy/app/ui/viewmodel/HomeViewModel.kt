package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ColocationResponse
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.data.model.ReportResponse
import com.habizy.app.data.model.RotationEntryResponse
import com.habizy.app.data.model.ShoppingItemResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.data.repository.ReceiptRepository
import com.habizy.app.data.repository.ReportRepository
import com.habizy.app.data.repository.RotationRepository
import com.habizy.app.data.repository.ShoppingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.habizy.app.util.userMessage

data class HomeData(
    val userName: String,
    val totalSpent: Double,
    val mySpent: Double,
    val memberCount: Int,
    val currentShopperName: String,
    val currentShopperColor: String?,
    val currentShopperInitial: String?,
    val shoppingItemCount: Int,
    val shoppingPreview: List<ShoppingItemResponse>,
    val daysUntilTurn: String,
    val isMyTurn: Boolean,
    val colocationId: String,
    val recentReports: List<ReportResponse>,
    val lastMyReceipt: ReceiptResponse? = null,
)

sealed class HomeState {
    data object Loading : HomeState()
    data object NoColocation : HomeState()
    data class Loaded(val data: HomeData) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val receiptRepository = ReceiptRepository(api)
    private val rotationRepository = RotationRepository(api)
    private val shoppingRepository = ShoppingRepository(api)
    private val reportRepository = ReportRepository(api)

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load()
    }

    fun load(isRefresh: Boolean = false, silent: Boolean = false) {
        if (silent && (_isRefreshing.value || _state.value is HomeState.Loading)) return
        viewModelScope.launch {
            if (!silent) {
                if (isRefresh) _isRefreshing.value = true else _state.value = HomeState.Loading
            }
            try {
                val meResult = authRepository.getMe()
                val me = meResult.getOrNull()
                if (me == null) {
                    if (!isRefresh && !silent) {
                        _state.value = HomeState.Error(meResult.exceptionOrNull()?.userMessage() ?: "Une erreur inattendue est survenue")
                    }
                    return@launch
                }

                val colocationResult = colocationRepository.getMyColocation()
                val colocationDetail = colocationResult.getOrNull()
                if (colocationDetail == null) {
                    if (!isRefresh && !silent) _state.value = HomeState.NoColocation
                    return@launch
                }

                val colocationId = colocationDetail.colocation.id
                val prev = (_state.value as? HomeState.Loaded)?.data

                // 15 s hard cap on secondary fetches — prevents infinite spinner on slow networks
                try {
                    withTimeout(15_000L) {
                        val statsDeferred = async { receiptRepository.getStats(colocationId) }
                        val rotationDeferred = async { rotationRepository.getRotation(colocationId) }
                        val shoppingDeferred = async { shoppingRepository.getList(colocationId) }
                        val reportsDeferred = async { reportRepository.getReports(colocationId) }
                        val receiptsDeferred = async { receiptRepository.getReceipts(colocationId) }

                        val statsResult = statsDeferred.await()
                        val rotationResult = rotationDeferred.await()
                        val shoppingResult = shoppingDeferred.await()
                        val reportsResult = reportsDeferred.await()
                        val receiptsResult = receiptsDeferred.await()

                        val stats = statsResult.getOrNull()
                        val rotation = rotationResult.getOrNull() ?: emptyList()
                        val shoppingItems = shoppingResult.getOrNull() ?: emptyList()
                        val reports = reportsResult.getOrNull() ?: emptyList()
                        val receipts = receiptsResult.getOrNull() ?: emptyList()

                        // Fall back to previous values so a partial failure never shows zeros
                        val totalSpent = stats?.totalSpent ?: prev?.totalSpent ?: 0.0
                        val mySpent = stats?.byRoommate
                            ?.find { it.user?.id == me.id }
                            ?.total ?: prev?.mySpent ?: 0.0

                        val currentShopper = rotation.firstOrNull { it.status == "current" }
                        val daysUntilTurn = computeDaysUntilTurn(me.id, rotation)
                        val isMyTurn = currentShopper?.user?.id == me.id

                        val uncheckedItems = shoppingItems.filter { !it.isChecked }
                        val shoppingItemCount = if (shoppingResult.isSuccess) uncheckedItems.size else prev?.shoppingItemCount ?: 0
                        val shoppingPreview = if (shoppingResult.isSuccess) uncheckedItems.take(3) else prev?.shoppingPreview ?: emptyList()
                        val recentReports = if (reportsResult.isSuccess) reports.take(3) else prev?.recentReports ?: emptyList()
                        val lastMyReceipt = if (receiptsResult.isSuccess) receipts.firstOrNull { it.user.id == me.id } else prev?.lastMyReceipt

                        _state.value = HomeState.Loaded(
                            HomeData(
                                userName = me.name,
                                totalSpent = totalSpent,
                                mySpent = mySpent,
                                memberCount = colocationDetail.members.size,
                                currentShopperName = currentShopper?.user?.name ?: prev?.currentShopperName ?: "",
                                currentShopperColor = currentShopper?.user?.colorHex ?: prev?.currentShopperColor,
                                currentShopperInitial = currentShopper?.user?.initial ?: prev?.currentShopperInitial,
                                shoppingItemCount = shoppingItemCount,
                                shoppingPreview = shoppingPreview,
                                daysUntilTurn = daysUntilTurn.ifEmpty { prev?.daysUntilTurn ?: "" },
                                isMyTurn = isMyTurn,
                                colocationId = colocationId,
                                recentReports = recentReports,
                                lastMyReceipt = if (!isMyTurn) lastMyReceipt else null,
                            )
                        )
                    }
                } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                    // Timeout: keep previous state, spinner will be dismissed by finally
                }
            } finally {
                if (!silent) _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return  // Prevent concurrent pull-to-refresh calls
        load(isRefresh = true)
    }

    fun silentRefresh() = load(silent = true)

    fun createColocation(name: String) {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            val result = colocationRepository.createColocation(name)
            result.onSuccess { colocation ->
                tokenManager.saveColocationId(colocation.id)
                load()
            }.onFailure { e ->
                _state.value = HomeState.Error(e.userMessage())
            }
        }
    }

    private fun computeDaysUntilTurn(
        userId: String,
        entries: List<RotationEntryResponse>
    ): String {
        val activeEntries = entries.filter { it.isDisabled != true }
        if (activeEntries.isEmpty()) return ""

        val currentIndex = activeEntries.indexOfFirst { it.status == "current" }
        if (currentIndex == -1) return ""
        val myIndex = activeEntries.indexOfFirst { it.user.id == userId }
        if (myIndex == -1) return ""

        val turnsAway = (myIndex - currentIndex + activeEntries.size) % activeEntries.size
        return when (turnsAway) {
            0 -> "c'est ton tour"
            1 -> "tu es le prochain"
            else -> "dans $turnsAway tours"
        }
    }
}

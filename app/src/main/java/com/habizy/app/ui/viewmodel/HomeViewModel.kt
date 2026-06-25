package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ColocationResponse
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
    val recentReports: List<ReportResponse>
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

            val meResult = authRepository.getMe()
            val me = meResult.getOrNull()
            if (me == null) {
                if (!isRefresh && !silent) {
                    _state.value = HomeState.Error(meResult.exceptionOrNull()?.userMessage() ?: "Une erreur inattendue est survenue")
                }
                if (!silent) _isRefreshing.value = false
                return@launch
            }

            val colocationResult = colocationRepository.getMyColocation()
            val colocationDetail = colocationResult.getOrNull()
            if (colocationDetail == null) {
                if (!isRefresh && !silent) _state.value = HomeState.NoColocation
                if (!silent) _isRefreshing.value = false
                return@launch
            }

            val colocationId = colocationDetail.colocation.id

            val statsDeferred = async { receiptRepository.getStats(colocationId) }
            val rotationDeferred = async { rotationRepository.getRotation(colocationId) }
            val shoppingDeferred = async { shoppingRepository.getList(colocationId) }
            val reportsDeferred = async { reportRepository.getReports(colocationId) }

            val stats = statsDeferred.await().getOrNull()
            val rotation = rotationDeferred.await().getOrNull() ?: emptyList()
            val shoppingItems = shoppingDeferred.await().getOrNull() ?: emptyList()
            val reports = reportsDeferred.await().getOrNull() ?: emptyList()

            val mySpent = stats?.byRoommate
                ?.find { it.user?.id == me.id }
                ?.total ?: 0.0

            val activeEntries = rotation.filter { it.isDisabled != true }
            val currentShopper = activeEntries.firstOrNull()
            val daysUntilTurn = computeDaysUntilTurn(me.id, activeEntries)
            val isMyTurn = currentShopper?.user?.id == me.id

            val uncheckedItems = shoppingItems.filter { !it.isChecked }

            _state.value = HomeState.Loaded(
                HomeData(
                    userName = me.name,
                    totalSpent = stats?.totalSpent ?: 0.0,
                    mySpent = mySpent,
                    memberCount = colocationDetail.members.size,
                    currentShopperName = currentShopper?.user?.name ?: "",
                    currentShopperColor = currentShopper?.user?.colorHex,
                    currentShopperInitial = currentShopper?.user?.initial,
                    shoppingItemCount = uncheckedItems.size,
                    shoppingPreview = uncheckedItems.take(3),
                    daysUntilTurn = daysUntilTurn,
                    isMyTurn = isMyTurn,
                    colocationId = colocationId,
                    recentReports = reports.take(3)
                )
            )
            if (!silent) _isRefreshing.value = false
        }
    }

    fun refresh() = load(isRefresh = true)
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
        activeEntries: List<RotationEntryResponse>
    ): String {
        if (activeEntries.isEmpty()) return ""

        val currentIndex = 0
        val myIndex = activeEntries.indexOfFirst { it.user.id == userId }
        if (myIndex == -1) return ""

        if (myIndex == currentIndex) return "c'est ton tour"
        if (myIndex == 1) return "tu es le prochain"

        val turnsAway = myIndex - currentIndex
        return "dans $turnsAway tours"
    }
}

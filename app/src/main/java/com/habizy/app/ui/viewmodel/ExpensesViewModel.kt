package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ExpenseStatsResponse
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.data.repository.ReceiptRepository
import com.habizy.app.data.repository.RotationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val receiptRepository = ReceiptRepository(api)
    private val rotationRepository = RotationRepository(api)

    private val _receipts = MutableStateFlow<List<ReceiptResponse>>(emptyList())
    val receipts: StateFlow<List<ReceiptResponse>> = _receipts.asStateFlow()

    private val _stats = MutableStateFlow<ExpenseStatsResponse?>(null)
    val stats: StateFlow<ExpenseStatsResponse?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isMyTurn = MutableStateFlow(false)
    val isMyTurn: StateFlow<Boolean> = _isMyTurn.asStateFlow()

    private val _currentPurchaserName = MutableStateFlow<String?>(null)
    val currentPurchaserName: StateFlow<String?> = _currentPurchaserName.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _gapThreshold = MutableStateFlow(50.0)
    val gapThreshold: StateFlow<Double> = _gapThreshold.asStateFlow()

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
                val colocationId = tokenManager.getColocationId()
                if (colocationId == null) {
                    if (!silent) _errorMessage.value = "Aucune colocation"
                    return@launch
                }

                val meDeferred = async { authRepository.getMe() }
                val receiptsDeferred = async { receiptRepository.getReceipts(colocationId) }
                val statsDeferred = async { receiptRepository.getStats(colocationId) }
                val rotationDeferred = async { rotationRepository.getRotation(colocationId) }
                val colocationDeferred = async { colocationRepository.getMyColocation() }

                val me = meDeferred.await().getOrNull()
                val receiptsResult = receiptsDeferred.await()
                val statsResult = statsDeferred.await()
                val rotationResult = rotationDeferred.await()
                val colocationResult = colocationDeferred.await()

                colocationResult.getOrNull()?.colocation?.spendingGapThreshold
                    ?.takeIf { it > 0 }
                    ?.let { _gapThreshold.value = it }

                receiptsResult.onSuccess {
                    _receipts.value = it.sortedWith(
                        compareByDescending<ReceiptResponse> { r -> r.date }
                            .thenByDescending { r -> r.time ?: "" }
                    )
                }.onFailure { if (!silent) _errorMessage.value = it.message ?: "Erreur chargement tickets" }

                statsResult.onSuccess { _stats.value = it }

                val rotation = rotationResult.getOrNull() ?: emptyList()
                val currentShopper = rotation.firstOrNull { it.status == "current" }
                _currentPurchaserName.value = currentShopper?.user?.name

                if (me != null) {
                    _isAdmin.value = me.isAdmin
                    _isMyTurn.value = currentShopper?.user?.id == me.id
                }
            } finally {
                if (!silent) {
                    if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
                }
            }
        }
    }

    fun refresh() = load(isRefresh = true)
    fun silentRefresh() = load(silent = true)

    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = receiptRepository.deleteReceipt(receiptId)
            result.onSuccess {
                _receipts.value = _receipts.value.filter { it.id != receiptId }
                _successMessage.value = "Ticket supprime"
                // Refresh stats after deletion
                val colocationId = tokenManager.getColocationId() ?: return@launch
                receiptRepository.getStats(colocationId).onSuccess { _stats.value = it }
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur lors de la suppression"
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

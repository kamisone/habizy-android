package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val receiptRepository = ReceiptRepository(ApiClient.apiService)

    private val _receipts = MutableStateFlow<List<ReceiptResponse>>(emptyList())
    val receipts: StateFlow<List<ReceiptResponse>> = _receipts.asStateFlow()

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

            val result = receiptRepository.getReceipts(colocationId)
            result.onSuccess {
                _receipts.value = it
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur chargement tickets"
            }

            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ColocationDetailResponse
import com.habizy.app.data.model.ColocationMemberResponse
import com.habizy.app.data.model.ReceiptResponse
import com.habizy.app.data.model.UserResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.data.repository.ReceiptRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val receiptRepository = ReceiptRepository(api)

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _colocation = MutableStateFlow<ColocationDetailResponse?>(null)
    val colocation: StateFlow<ColocationDetailResponse?> = _colocation.asStateFlow()

    private val _members = MutableStateFlow<List<ColocationMemberResponse>>(emptyList())
    val members: StateFlow<List<ColocationMemberResponse>> = _members.asStateFlow()

    private val _receipts = MutableStateFlow<List<ReceiptResponse>>(emptyList())
    val receipts: StateFlow<List<ReceiptResponse>> = _receipts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val myTotalSpent: Double
        get() {
            val userId = _user.value?.id ?: return 0.0
            return _receipts.value
                .filter { it.user.id == userId }
                .sumOf { it.totalAmount }
        }

    val ticketCount: Int
        get() {
            val userId = _user.value?.id ?: return 0
            return _receipts.value.count { it.user.id == userId }
        }

    val isAdmin: Boolean
        get() = _user.value?.isAdmin == true

    val spendingGapThreshold: Double?
        get() = _colocation.value?.colocation?.spendingGapThreshold

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

            val meDeferred = async { authRepository.getMe() }
            val colocationDeferred = async { colocationRepository.getMyColocation() }

            val me = meDeferred.await().getOrNull()
            val colocationDetail = colocationDeferred.await().getOrNull()

            if (me != null) {
                _user.value = me
            } else {
                if (!silent) {
                    _errorMessage.value = "Impossible de charger le profil"
                    if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
                }
                return@launch
            }

            if (colocationDetail != null) {
                _colocation.value = colocationDetail
                _members.value = colocationDetail.members

                val colocationId = colocationDetail.colocation.id
                val receiptsResult = receiptRepository.getReceipts(colocationId)
                receiptsResult.onSuccess { _receipts.value = it }
            }

            if (!silent) {
                if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
            }
        }
    }

    fun refresh() = load(isRefresh = true)
    fun silentRefresh() = load(silent = true)

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.changePassword(currentPassword, newPassword)
            result.onSuccess {
                _successMessage.value = "Mot de passe modifie"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur lors du changement de mot de passe"
            }
            _isLoading.value = false
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.updateName(name)
            result.onSuccess { updatedUser ->
                _user.value = updatedUser
                _successMessage.value = "Nom mis a jour"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur lors de la mise a jour du nom"
            }
            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

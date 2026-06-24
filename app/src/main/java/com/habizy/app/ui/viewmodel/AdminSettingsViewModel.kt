package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ColocationDetailResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.data.repository.RotationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val rotationRepository = RotationRepository(api)

    private val _colocation = MutableStateFlow<ColocationDetailResponse?>(null)
    val colocation: StateFlow<ColocationDetailResponse?> = _colocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _spendingGapThreshold = MutableStateFlow("")
    val spendingGapThreshold: StateFlow<String> = _spendingGapThreshold.asStateFlow()

    private val _colocationName = MutableStateFlow("")
    val colocationName: StateFlow<String> = _colocationName.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = colocationRepository.getMyColocation()
            result.onSuccess { detail ->
                _colocation.value = detail
                _colocationName.value = detail.colocation.name
                _spendingGapThreshold.value = detail.colocation.spendingGapThreshold?.toString() ?: ""
                _notificationsEnabled.value = detail.colocation.notificationsEnabled != false
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur chargement parametres"
            }

            _isLoading.value = false
        }
    }

    fun setColocationName(name: String) {
        _colocationName.value = name
    }

    fun setSpendingGapThreshold(value: String) {
        _spendingGapThreshold.value = value
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun saveSettings() {
        viewModelScope.launch {
            val colocationId = _colocation.value?.colocation?.id
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                return@launch
            }

            _isSaving.value = true
            _errorMessage.value = null

            val threshold = _spendingGapThreshold.value.toDoubleOrNull()
            val result = colocationRepository.updateColocation(
                id = colocationId,
                name = _colocationName.value.ifBlank { null },
                spendingGapThreshold = threshold,
                notificationsEnabled = _notificationsEnabled.value
            )

            result.onSuccess {
                _successMessage.value = "Parametres sauvegardes"
                load()
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur sauvegarde parametres"
            }

            _isSaving.value = false
        }
    }

    fun generateRotation() {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val colocationId = _colocation.value?.colocation?.id
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                _isSaving.value = false
                return@launch
            }

            val result = rotationRepository.generate(colocationId)
            result.onSuccess {
                _successMessage.value = "Rotation generee"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur generation rotation"
            }

            _isSaving.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

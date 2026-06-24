package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.RotationEntryResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.data.repository.RotationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RotationViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val rotationRepository = RotationRepository(api)

    private val _entries = MutableStateFlow<List<RotationEntryResponse>>(emptyList())
    val entries: StateFlow<List<RotationEntryResponse>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _hasReordered = MutableStateFlow(false)
    val hasReordered: StateFlow<Boolean> = _hasReordered.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val myEntry: RotationEntryResponse?
        get() {
            val userId = _currentUserId.value ?: return null
            return _entries.value.find { it.user.id == userId }
        }

    val isMyTurn: Boolean
        get() {
            val userId = _currentUserId.value ?: return false
            val activeEntries = _entries.value.filter { it.isDisabled != true }
            return activeEntries.firstOrNull()?.user?.id == userId
        }

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

            val meDeferred = async { authRepository.getMe() }
            val rotationDeferred = async { rotationRepository.getRotation(colocationId) }

            val me = meDeferred.await().getOrNull()
            val rotationResult = rotationDeferred.await()

            if (me != null) {
                _currentUserId.value = me.id
                _isAdmin.value = me.isAdmin
            }

            rotationResult.onSuccess {
                _entries.value = it
                _hasReordered.value = false
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur chargement rotation"
            }

            _isLoading.value = false
        }
    }

    fun generate() {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                _isSaving.value = false
                return@launch
            }

            val result = rotationRepository.generate(colocationId)
            result.onSuccess {
                _entries.value = it
                _hasReordered.value = false
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur generation rotation"
            }

            _isSaving.value = false
        }
    }

    fun moveEntry(fromIndex: Int, toIndex: Int) {
        val current = _entries.value.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _entries.value = current
        _hasReordered.value = true
    }

    fun toggleMemberActive(userId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                return@launch
            }

            val result = colocationRepository.toggleMemberActive(colocationId, userId)
            result.onSuccess {
                load()
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur mise a jour membre"
            }
        }
    }

    fun saveOrder() {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                _isSaving.value = false
                return@launch
            }

            val userIds = _entries.value.map { it.user.id }
            val result = colocationRepository.setPurchaseOrder(colocationId, userIds)
            result.onSuccess {
                _hasReordered.value = false
                load()
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur sauvegarde ordre"
            }

            _isSaving.value = false
        }
    }

    fun swap(theirEntryId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val myEntryId = myEntry?.id
            if (myEntryId == null) {
                _errorMessage.value = "Impossible de trouver votre tour"
                _isSaving.value = false
                return@launch
            }

            val result = rotationRepository.swap(myEntryId, theirEntryId)
            result.onSuccess {
                _entries.value = it
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur echange rotation"
            }

            _isSaving.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

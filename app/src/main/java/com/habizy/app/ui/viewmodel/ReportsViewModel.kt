package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.ReportResponse
import com.habizy.app.data.model.ReportTagResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.ReportRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)
    private val reportRepository = ReportRepository(api)

    private val _reports = MutableStateFlow<List<ReportResponse>>(emptyList())
    val reports: StateFlow<List<ReportResponse>> = _reports.asStateFlow()

    private val _tags = MutableStateFlow<List<ReportTagResponse>>(emptyList())
    val tags: StateFlow<List<ReportTagResponse>> = _tags.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _tagFilter = MutableStateFlow<String?>(null)
    val tagFilter: StateFlow<String?> = _tagFilter.asStateFlow()

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

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                if (!silent) {
                    _errorMessage.value = "Aucune colocation"
                    if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
                }
                return@launch
            }

            val meDeferred = async { authRepository.getMe() }
            val reportsDeferred = async { reportRepository.getReports(colocationId, _tagFilter.value) }
            val tagsDeferred = async { reportRepository.getTags(colocationId) }

            val me = meDeferred.await().getOrNull()
            if (me != null) _isAdmin.value = me.isAdmin

            reportsDeferred.await()
                .onSuccess { _reports.value = it }
                .onFailure { e -> if (!silent) _errorMessage.value = e.message ?: "Erreur chargement signalements" }

            tagsDeferred.await().onSuccess { _tags.value = it }

            if (!silent) {
                if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
            }
        }
    }

    fun refresh() = load(isRefresh = true)
    fun silentRefresh() = load(silent = true)

    fun setTagFilter(tag: String?) {
        _tagFilter.value = tag
        viewModelScope.launch {
            val colocationId = tokenManager.getColocationId() ?: return@launch
            _isLoading.value = true
            val result = reportRepository.getReports(colocationId, tag)
            result.onSuccess { _reports.value = it }
                .onFailure { e -> _errorMessage.value = e.message ?: "Erreur filtrage" }
            _isLoading.value = false
        }
    }

    fun createTag(title: String, color: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                return@launch
            }

            val result = reportRepository.createTag(title, color, colocationId)
            result.onSuccess { newTag ->
                _tags.value = _tags.value + newTag
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur creation tag"
            }
        }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val result = reportRepository.deleteTag(tagId)
            result.onSuccess {
                _tags.value = _tags.value.filter { it.id != tagId }
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur suppression tag"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

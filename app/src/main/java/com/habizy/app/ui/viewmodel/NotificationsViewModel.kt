package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.model.NotificationResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationRepository = NotificationRepository(ApiClient.apiService)

    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val unreadCount: Int
        get() = _notifications.value.count { !it.isRead }

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = notificationRepository.getAll()
            result.onSuccess {
                _notifications.value = it
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur chargement notifications"
            }

            _isLoading.value = false
        }
    }

    fun markRead(notification: NotificationResponse) {
        if (notification.isRead) return

        viewModelScope.launch {
            val result = notificationRepository.markRead(notification.id)
            result.onSuccess {
                _notifications.value = _notifications.value.map { n ->
                    if (n.id == notification.id) n.copy(isRead = true) else n
                }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val result = notificationRepository.markAllRead()
            result.onSuccess {
                _notifications.value = _notifications.value.map { n ->
                    n.copy(isRead = true)
                }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

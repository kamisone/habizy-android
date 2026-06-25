package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.remote.RegisterDeviceRequest
import com.habizy.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.habizy.app.util.userMessage
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)

    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading.asStateFlow()

    private val _isJoinLoading = MutableStateFlow(false)
    val isJoinLoading: StateFlow<Boolean> = _isJoinLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _joinError = MutableStateFlow<String?>(null)
    val joinError: StateFlow<String?> = _joinError.asStateFlow()

    val isLoggedIn = tokenManager.isLoggedIn
    val profileCompleted = tokenManager.profileCompleted

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginError.value = null
            val result = authRepository.login(email, password)
            result.onSuccess {
                registerFcmDevice()
            }.onFailure { e ->
                _loginError.value = e.userMessage()
            }
            _isLoginLoading.value = false
        }
    }

    fun joinColocation(inviteCode: String) {
        viewModelScope.launch {
            _isJoinLoading.value = true
            _joinError.value = null
            val result = authRepository.joinColocation(inviteCode)
            result.onFailure { e ->
                _joinError.value = e.userMessage()
            }
            _isJoinLoading.value = false
        }
    }

    fun completeProfile(
        name: String,
        email: String,
        password: String,
        colorHex: String? = null,
        phone: String? = null
    ) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginError.value = null
            val result = authRepository.completeProfile(name, email, password, colorHex, phone)
            result.onFailure { e ->
                _loginError.value = e.userMessage()
            }
            _isLoginLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            unregisterFcmDevice()
            authRepository.logout()
        }
    }

    fun deleteAccountAndLogout() {
        viewModelScope.launch {
            unregisterFcmDevice()
            val result = authRepository.deleteMe()
            result.onFailure { e ->
                _loginError.value = e.userMessage()
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearJoinError() {
        _joinError.value = null
    }

    private suspend fun registerFcmDevice() {
        try {
            val fcmToken = tokenManager.getFcmToken()
            if (fcmToken != null) {
                api.registerDevice(RegisterDeviceRequest(platform = "android", token = fcmToken))
            }
        } catch (_: Exception) {
            // Silently ignore device registration failures
        }
    }

    private suspend fun unregisterFcmDevice() {
        try {
            val fcmToken = tokenManager.getFcmToken()
            if (fcmToken != null) {
                api.unregisterDevice(fcmToken)
            }
        } catch (_: Exception) {
            // Silently ignore device unregistration failures
        }
    }
}

package com.habizy.app.data.repository

import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(LoginRequest(email, password))
        tokenManager.saveAuthResponse(response)
        response
    }

    suspend fun joinColocation(inviteCode: String): Result<AuthResponse> = runCatching {
        val response = api.joinColocation(JoinColocationRequest(inviteCode))
        tokenManager.saveTokens(response.accessToken, response.refreshToken)
        if (response.profileCompleted != null) {
            tokenManager.saveProfileCompleted(response.profileCompleted)
        } else {
            tokenManager.saveProfileCompleted(false)
        }
        response
    }

    suspend fun completeProfile(
        name: String,
        email: String,
        password: String,
        colorHex: String? = null,
        phone: String? = null
    ): Result<AuthResponse> = runCatching {
        val body = CompleteProfileRequest(name, email, password, colorHex, phone)
        val response = api.completeProfile(body)
        tokenManager.saveAuthResponse(response)
        tokenManager.saveProfileCompleted(true)
        response
    }

    suspend fun getMe(): Result<UserResponse> = runCatching {
        api.getMe()
    }

    suspend fun changePassword(current: String, new: String): Result<Unit> = runCatching {
        api.changePassword(ChangePasswordRequest(current, new))
    }

    suspend fun updateName(name: String): Result<UserResponse> = runCatching {
        api.updateName(UpdateNameRequest(name))
    }

    suspend fun deleteMe(): Result<Unit> = runCatching {
        api.deleteMe()
        tokenManager.clear()
    }

    suspend fun logout() {
        tokenManager.clear()
    }
}

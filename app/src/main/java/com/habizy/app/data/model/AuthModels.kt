package com.habizy.app.data.model

import com.google.gson.annotations.SerializedName

// -- Requests --

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class CompleteProfileRequest(
    val name: String,
    val email: String,
    val password: String,
    val colorHex: String? = null,
    val phone: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UpdateNameRequest(
    val name: String
)

// -- Responses --

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val profileCompleted: Boolean? = null
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val colorHex: String? = null,
    val initial: String? = null,
    val phone: String? = null,
    val isAdmin: Boolean = false,
    val profileCompleted: Boolean? = null
)

data class CreateUserResponse(
    val user: UserResponse,
    val generatedPassword: String? = null
)

data class ToggleAdminResponse(
    val isAdmin: Boolean
)

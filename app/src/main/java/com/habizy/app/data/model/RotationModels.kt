package com.habizy.app.data.model

// -- Requests --

data class SwapRotationRequest(
    val myRotationId: String,
    val theirRotationId: String
)

// -- Responses --

data class RotationEntryResponse(
    val id: String,
    val user: UserResponse,
    val weekStart: String? = null,
    val weekEnd: String? = null,
    val orderIndex: Int,
    val status: String? = null,
    val isDisabled: Boolean? = null
)

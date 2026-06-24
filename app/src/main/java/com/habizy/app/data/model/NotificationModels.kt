package com.habizy.app.data.model

// -- Responses --

data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String? = null,
    val body: String? = null,
    val message: String,
    val isRead: Boolean,
    val readAt: String? = null,
    val actor: UserResponse? = null,
    val data: Map<String, String>? = null,
    val createdAt: String? = null
)

data class UnreadCountResponse(
    val count: Int
)

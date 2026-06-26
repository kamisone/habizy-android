package com.habizy.app.data.repository

import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService
import com.habizy.app.data.remote.RegisterDeviceRequest

class NotificationRepository(private val api: ApiService) {

    suspend fun registerDevice(platform: String, token: String): Result<Unit> = runCatching {
        api.registerDevice(RegisterDeviceRequest(platform = platform, fcmToken = token))
    }

    suspend fun unregisterDevice(token: String): Result<Unit> = runCatching {
        api.unregisterDevice(token)
    }

    suspend fun getAll(): Result<List<NotificationResponse>> = runCatching {
        api.getNotifications()
    }

    suspend fun getUnreadCount(): Result<UnreadCountResponse> = runCatching {
        api.getUnreadCount()
    }

    suspend fun markRead(id: String): Result<Unit> = runCatching {
        api.markNotificationRead(id)
    }

    suspend fun markAllRead(): Result<Unit> = runCatching {
        api.markAllNotificationsRead()
    }
}

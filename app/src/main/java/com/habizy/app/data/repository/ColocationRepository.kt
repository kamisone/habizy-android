package com.habizy.app.data.repository

import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class ColocationRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getMyColocation(): Result<ColocationDetailResponse> = runCatching {
        val detail = api.getMyColocation()
        tokenManager.saveColocationId(detail.colocation.id)
        detail
    }

    suspend fun createColocation(name: String): Result<ColocationResponse> = runCatching {
        api.createColocation(CreateColocationRequest(name))
    }

    suspend fun updateColocation(
        id: String,
        name: String? = null,
        spendingGapThreshold: Double? = null,
        notificationsEnabled: Boolean? = null
    ): Result<ColocationResponse> = runCatching {
        api.updateColocation(id, UpdateColocationRequest(name, spendingGapThreshold, notificationsEnabled))
    }

    suspend fun addMember(
        colocationId: String,
        name: String,
        email: String,
        password: String? = null,
        colorHex: String? = null
    ): Result<CreateUserResponse> = runCatching {
        api.addMember(colocationId, AddMemberRequest(name, email, password, colorHex))
    }

    suspend fun removeMember(colocationId: String, userId: String): Result<Unit> = runCatching {
        api.removeMember(colocationId, userId)
    }

    suspend fun setPurchaseOrder(colocationId: String, userIds: List<String>): Result<ColocationResponse> = runCatching {
        api.setPurchaseOrder(colocationId, SetPurchaseOrderRequest(userIds))
    }

    suspend fun toggleMemberActive(colocationId: String, userId: String): Result<Unit> = runCatching {
        api.toggleMemberActive(colocationId, userId)
    }
}

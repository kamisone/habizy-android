package com.habizy.app.data.repository

import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class RotationRepository(private val api: ApiService) {

    suspend fun getRotation(colocationId: String): Result<List<RotationEntryResponse>> = runCatching {
        api.getRotation(colocationId)
    }

    suspend fun generate(colocationId: String): Result<List<RotationEntryResponse>> = runCatching {
        api.generateRotation(colocationId)
    }

    suspend fun swap(myId: String, theirId: String): Result<List<RotationEntryResponse>> = runCatching {
        api.swapRotation(SwapRotationRequest(myId, theirId))
    }
}

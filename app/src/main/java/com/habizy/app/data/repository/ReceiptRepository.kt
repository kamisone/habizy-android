package com.habizy.app.data.repository

import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class ReceiptRepository(private val api: ApiService) {

    suspend fun getReceipts(colocationId: String): Result<List<ReceiptResponse>> = runCatching {
        api.getReceipts(colocationId)
    }

    suspend fun createReceipt(
        colocationId: String,
        store: String,
        date: String,
        time: String? = null,
        totalAmount: Double,
        photoUrl: String? = null,
        items: List<CreateReceiptItemRequest>
    ): Result<ReceiptResponse> = runCatching {
        api.createReceipt(
            CreateReceiptRequest(colocationId, store, date, time, totalAmount, photoUrl, items)
        )
    }

    suspend fun getStats(colocationId: String): Result<ExpenseStatsResponse> = runCatching {
        api.getExpenseStats(colocationId)
    }

    suspend fun deleteReceipt(id: String): Result<Unit> = runCatching {
        api.deleteReceipt(id)
    }

    suspend fun getArticleCatalog(colocationId: String): Result<List<ArticleSuggestion>> = runCatching {
        api.getArticleCatalog(colocationId)
    }

    suspend fun getArticleStats(colocationId: String): Result<List<ArticleStat>> = runCatching {
        api.getArticleStats(colocationId)
    }
}

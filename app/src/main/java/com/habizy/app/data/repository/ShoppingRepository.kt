package com.habizy.app.data.repository

import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class ShoppingRepository(private val api: ApiService) {

    suspend fun getList(colocationId: String): Result<List<ShoppingItemResponse>> = runCatching {
        api.getShoppingList(colocationId)
    }

    suspend fun addItem(
        name: String,
        quantity: Int,
        assigneeId: String? = null,
        colocationId: String
    ): Result<ShoppingItemResponse> = runCatching {
        api.createShoppingItem(
            CreateShoppingItemRequest(name, quantity, assigneeId, colocationId)
        )
    }

    suspend fun toggle(id: String): Result<ShoppingItemResponse> = runCatching {
        api.toggleShoppingItem(id)
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        api.deleteShoppingItem(id)
    }
}

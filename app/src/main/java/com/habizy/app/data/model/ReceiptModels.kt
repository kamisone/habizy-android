package com.habizy.app.data.model

// -- Requests --

data class CreateReceiptItemRequest(
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String
)

data class CreateReceiptRequest(
    val colocationId: String,
    val store: String,
    val date: String,
    val time: String? = null,
    val totalAmount: Double,
    val photoUrl: String? = null,
    val items: List<CreateReceiptItemRequest>
)

// -- Responses --

data class ReceiptItemResponse(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String
)

data class ReceiptResponse(
    val id: String,
    val store: String,
    val date: String,
    val time: String? = null,
    val totalAmount: Double,
    val photoUrl: String? = null,
    val user: UserResponse,
    val items: List<ReceiptItemResponse>,
    val createdAt: String? = null
)

data class ArticleSuggestion(
    val name: String,
    val category: String,
    val lastPrice: Double
)

data class ArticleStat(
    val name: String,
    val category: String,
    val totalAmount: Double,
    val totalQuantity: Int,
    val fraction: Double
)

data class CategoryStat(
    val category: String,
    val total: Double,
    val fraction: Double
)

data class RoommateStat(
    val user: UserResponse? = null,
    val total: Double,
    val fraction: Double
)

data class ExpenseStatsResponse(
    val totalSpent: Double,
    val byCategory: List<CategoryStat>,
    val byRoommate: List<RoommateStat>
)

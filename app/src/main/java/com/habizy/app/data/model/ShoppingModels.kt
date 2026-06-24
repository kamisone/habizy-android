package com.habizy.app.data.model

// -- Requests --

data class CreateShoppingItemRequest(
    val name: String,
    val quantity: Int,
    val assigneeId: String? = null,
    val colocationId: String
)

// -- Responses --

data class ShoppingItemResponse(
    val id: String,
    val name: String,
    val quantity: Int,
    val isChecked: Boolean,
    val assignee: UserResponse? = null,
    val checkedBy: UserResponse? = null,
    val createdAt: String? = null
)

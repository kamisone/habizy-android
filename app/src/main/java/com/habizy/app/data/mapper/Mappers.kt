package com.habizy.app.data.mapper

import androidx.compose.ui.graphics.Color
import com.habizy.app.data.*
import com.habizy.app.data.model.*

// -- Color parsing helper --

private fun parseColorHex(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFF6366F1) // default indigo
    return try {
        val cleaned = hex.removePrefix("#")
        Color(("FF$cleaned").toLong(16))
    } catch (_: Exception) {
        Color(0xFF6366F1)
    }
}

// -- UserResponse -> Roommate --

fun UserResponse.toRoommate(): Roommate = Roommate(
    id = id,
    name = name,
    initial = initial ?: name.firstOrNull()?.uppercase() ?: "?",
    color = parseColorHex(colorHex)
)

// -- ShoppingItemResponse -> ShoppingItem --

fun ShoppingItemResponse.toShoppingItem(): ShoppingItem = ShoppingItem(
    id = id,
    name = name,
    quantity = quantity,
    isChecked = isChecked,
    assignee = assignee?.toRoommate(),
    checkedBy = checkedBy?.toRoommate()
)

fun List<ShoppingItemResponse>.toShoppingItems(): List<ShoppingItem> = map { it.toShoppingItem() }

// -- ReceiptResponse -> Expense --

fun ReceiptResponse.toExpense(): Expense = Expense(
    id = id,
    store = store,
    date = date,
    totalAmount = totalAmount,
    user = user.toRoommate(),
    itemCount = items.size,
    photoUrl = photoUrl
)

fun List<ReceiptResponse>.toExpenses(): List<Expense> = map { it.toExpense() }

// -- ContributionStatusItem -> ContributionStatus --

fun ContributionStatusItem.toContributionStatus(): ContributionStatus = ContributionStatus(
    user = user.toRoommate(),
    hasPaid = hasPaid
)

// -- RotationEntryResponse -> RotationEntry --

fun RotationEntryResponse.toRotationEntry(): RotationEntry = RotationEntry(
    id = id,
    user = user.toRoommate(),
    weekStart = weekStart,
    weekEnd = weekEnd,
    orderIndex = orderIndex,
    status = status,
    isDisabled = isDisabled ?: false
)

fun List<RotationEntryResponse>.toRotationEntries(): List<RotationEntry> = map { it.toRotationEntry() }

// -- NotificationResponse -> NotificationItem --

fun NotificationResponse.toNotificationItem(): NotificationItem = NotificationItem(
    id = id,
    type = type,
    title = title,
    body = body,
    message = message,
    isRead = isRead,
    actorName = actor?.name,
    actorInitial = actor?.initial ?: actor?.name?.firstOrNull()?.uppercase(),
    actorColor = actor?.let { parseColorHex(it.colorHex) },
    createdAt = createdAt
)

fun List<NotificationResponse>.toNotificationItems(): List<NotificationItem> = map { it.toNotificationItem() }

// -- CategoryStat -> ExpenseCategory --

fun CategoryStat.toExpenseCategory(): ExpenseCategory = ExpenseCategory(
    category = category,
    total = total,
    fraction = fraction
)

// -- RoommateStat -> RoommateSpending --

fun RoommateStat.toRoommateSpending(): RoommateSpending = RoommateSpending(
    roommate = user?.toRoommate(),
    total = total,
    fraction = fraction
)

// -- ExpenseStatsResponse --

fun ExpenseStatsResponse.toCategoryList(): List<ExpenseCategory> =
    byCategory.map { it.toExpenseCategory() }

fun ExpenseStatsResponse.toRoommateSpendingList(): List<RoommateSpending> =
    byRoommate.map { it.toRoommateSpending() }

// -- ColocationMemberResponse -> Roommate --

fun ColocationMemberResponse.toRoommate(): Roommate = user.toRoommate()

fun List<ColocationMemberResponse>.toRoommates(): List<Roommate> = map { it.toRoommate() }

// -- MenageBoardMember -> Roommate (lightweight) --

fun MenageBoardMember.toRoommate(): Roommate = Roommate(
    id = userId,
    name = name,
    initial = initial ?: name.firstOrNull()?.uppercase() ?: "?",
    color = parseColorHex(colorHex)
)

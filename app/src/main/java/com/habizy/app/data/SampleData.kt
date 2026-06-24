package com.habizy.app.data

import androidx.compose.ui.graphics.Color

// -- UI data classes --

data class Roommate(
    val id: String,
    val name: String,
    val initial: String,
    val color: Color
)

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val isChecked: Boolean,
    val assignee: Roommate? = null,
    val checkedBy: Roommate? = null
)

data class Expense(
    val id: String,
    val store: String,
    val date: String,
    val totalAmount: Double,
    val user: Roommate,
    val itemCount: Int,
    val photoUrl: String? = null
)

data class ContributionStatus(
    val user: Roommate,
    val hasPaid: Boolean
)

data class RotationEntry(
    val id: String,
    val user: Roommate,
    val weekStart: String? = null,
    val weekEnd: String? = null,
    val orderIndex: Int,
    val status: String? = null,
    val isDisabled: Boolean = false
)

data class NotificationItem(
    val id: String,
    val type: String,
    val title: String?,
    val body: String?,
    val message: String,
    val isRead: Boolean,
    val actorName: String? = null,
    val actorInitial: String? = null,
    val actorColor: Color? = null,
    val createdAt: String? = null
)

data class ExpenseCategory(
    val category: String,
    val total: Double,
    val fraction: Double
)

data class RoommateSpending(
    val roommate: Roommate?,
    val total: Double,
    val fraction: Double
)

// -- Sample / preview data --

object SampleData {

    val roommates = listOf(
        Roommate("1", "Alice", "A", Color(0xFF6366F1)),
        Roommate("2", "Bob", "B", Color(0xFFF59E0B)),
        Roommate("3", "Charlie", "C", Color(0xFF10B981)),
        Roommate("4", "Diana", "D", Color(0xFFEF4444))
    )

    val shoppingItems = listOf(
        ShoppingItem("s1", "Lait", 2, false, roommates[0]),
        ShoppingItem("s2", "Pain", 1, true, roommates[1], roommates[1]),
        ShoppingItem("s3", "Oeufs", 1, false, roommates[2]),
        ShoppingItem("s4", "Beurre", 1, false)
    )

    val expenses = listOf(
        Expense("e1", "Carrefour", "2024-03-15", 45.30, roommates[0], 5),
        Expense("e2", "Lidl", "2024-03-14", 32.10, roommates[1], 3),
        Expense("e3", "Monoprix", "2024-03-13", 18.50, roommates[2], 2)
    )

    val contributionStatuses = listOf(
        ContributionStatus(roommates[0], true),
        ContributionStatus(roommates[1], true),
        ContributionStatus(roommates[2], false),
        ContributionStatus(roommates[3], false)
    )

    val rotationEntries = listOf(
        RotationEntry("r1", roommates[0], "2024-03-18", "2024-03-24", 0, "current"),
        RotationEntry("r2", roommates[1], "2024-03-25", "2024-03-31", 1, "upcoming"),
        RotationEntry("r3", roommates[2], "2024-04-01", "2024-04-07", 2, "upcoming"),
        RotationEntry("r4", roommates[3], "2024-04-08", "2024-04-14", 3, "upcoming")
    )

    val notifications = listOf(
        NotificationItem(
            "n1", "shopping", "Courses", null,
            "Alice a ajouté Lait à la liste",
            false, "Alice", "A", Color(0xFF6366F1), "2024-03-15T10:00:00Z"
        ),
        NotificationItem(
            "n2", "receipt", "Ticket", null,
            "Bob a ajouté un ticket de 32.10 EUR",
            true, "Bob", "B", Color(0xFFF59E0B), "2024-03-14T18:30:00Z"
        )
    )

    val expenseCategories = listOf(
        ExpenseCategory("Alimentation", 65.80, 0.55),
        ExpenseCategory("Hygiène", 25.00, 0.21),
        ExpenseCategory("Ménage", 15.10, 0.13),
        ExpenseCategory("Autre", 14.00, 0.11)
    )

    val roommateSpending = listOf(
        RoommateSpending(roommates[0], 45.30, 0.38),
        RoommateSpending(roommates[1], 32.10, 0.27),
        RoommateSpending(roommates[2], 18.50, 0.15),
        RoommateSpending(roommates[3], 24.00, 0.20)
    )
}

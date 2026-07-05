package com.habizy.app.data.model

// -- Requests --

data class CreateColocationRequest(
    val name: String
)

data class JoinColocationRequest(
    val inviteCode: String,
    val name: String,
    val email: String,
    val password: String? = null
)

data class UpdateColocationRequest(
    val name: String? = null,
    val spendingGapThreshold: Double? = null,
    val notificationsEnabled: Boolean? = null
)

data class AddMemberRequest(
    val name: String,
    val email: String,
    val password: String? = null,
    val colorHex: String? = null
)

data class SetPurchaseOrderRequest(
    val userIds: List<String>
)

// -- Responses --

data class ColocationResponse(
    val id: String,
    val name: String,
    val inviteCode: String? = null,
    val createdAt: String? = null,
    val spendingGapThreshold: Double? = null,
    val notificationsEnabled: Boolean? = null
)

data class ColocationMemberResponse(
    val id: String,
    val role: String,
    val user: UserResponse,
    val joinedAt: String? = null
)

data class ColocationDetailResponse(
    val colocation: ColocationResponse,
    val members: List<ColocationMemberResponse>
)

// -- Contribution --

data class CreateContributionRequest(
    val colocationId: String,
    val amount: Double? = null
)

data class ContributionResponse(
    val id: String,
    val amount: Double,
    val cycleNumber: Int? = null,
    val user: UserResponse,
    val createdAt: String? = null
)

data class ContributionStatusItem(
    val user: UserResponse,
    val hasPaid: Boolean
)

data class CycleStatusResponse(
    val cycleNumber: Int,
    val paidCount: Int,
    val totalMembers: Int,
    val contributions: List<ContributionStatusItem>
)

package com.habizy.app.data.model

// -- Requests --

data class CreateReportRequest(
    val colocationId: String,
    val title: String,
    val description: String? = null,
    val tags: List<String>? = null,
    val photoUrls: List<String>? = null
)

data class UpdateReportRequest(
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val photoUrls: List<String>? = null
)

data class CreateReportCommentRequest(
    val content: String
)

data class CreateTagRequest(
    val title: String,
    val color: String,
    val colocationId: String
)

// -- Responses --

data class TagDetail(
    val title: String,
    val color: String? = null,
)

data class ReportResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val tags: List<String>? = null,
    val tagDetails: List<TagDetail>? = null,
    val photoUrls: List<String>? = null,
    val user: UserResponse,
    val colocationId: String,
    val commentCount: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ReportCommentResponse(
    val id: String,
    val content: String,
    val user: UserResponse,
    val createdAt: String? = null
)

data class ReportDetailResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    val tags: List<String>? = null,
    val tagDetails: List<TagDetail>? = null,
    val photoUrls: List<String>? = null,
    val user: UserResponse,
    val colocationId: String,
    val comments: List<ReportCommentResponse>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ReportTagResponse(
    val id: String,
    val title: String,
    val color: String,
    val colocationId: String
)

package com.habizy.app.data.repository

import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class ReportRepository(private val api: ApiService) {

    suspend fun getReports(
        colocationId: String,
        tag: String? = null
    ): Result<List<ReportResponse>> = runCatching {
        api.getReports(colocationId, tag)
    }

    suspend fun create(body: CreateReportRequest): Result<ReportResponse> = runCatching {
        api.createReport(body)
    }

    suspend fun getDetail(id: String): Result<ReportDetailResponse> = runCatching {
        api.getReportDetail(id)
    }

    suspend fun update(id: String, body: UpdateReportRequest): Result<ReportDetailResponse> = runCatching {
        api.updateReport(id, body)
    }

    suspend fun addComment(id: String, content: String): Result<ReportCommentResponse> = runCatching {
        api.createReportComment(id, CreateReportCommentRequest(content))
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        api.deleteReport(id)
    }

    suspend fun getTags(colocationId: String): Result<List<ReportTagResponse>> = runCatching {
        api.getReportTags(colocationId)
    }

    suspend fun createTag(title: String, color: String, colocationId: String): Result<ReportTagResponse> = runCatching {
        api.createReportTag(CreateTagRequest(title, color, colocationId))
    }

    suspend fun deleteTag(id: String): Result<Unit> = runCatching {
        api.deleteReportTag(id)
    }
}

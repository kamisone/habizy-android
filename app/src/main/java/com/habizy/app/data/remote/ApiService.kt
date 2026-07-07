package com.habizy.app.data.remote

import com.habizy.app.data.model.*
import retrofit2.http.*

interface ApiService {

    // -- Auth --

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): CreateUserResponse

    @POST("auth/register-admin")
    suspend fun registerAdmin(@Body body: RegisterRequest): CreateUserResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest)

    @GET("auth/me")
    suspend fun getMe(): UserResponse

    @PATCH("auth/me")
    suspend fun updateName(@Body body: UpdateNameRequest): UserResponse

    @DELETE("auth/me")
    suspend fun deleteMe()

    @PATCH("auth/profile")
    suspend fun completeProfile(@Body body: CompleteProfileRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): AuthResponse

    @PATCH("auth/users/{userId}/toggle-admin")
    suspend fun toggleAdmin(@Path("userId") userId: String): ToggleAdminResponse

    // -- Colocation --

    @POST("colocations")
    suspend fun createColocation(@Body body: CreateColocationRequest): ColocationResponse

    @GET("colocations/mine")
    suspend fun getMyColocation(): ColocationDetailResponse

    @POST("colocations/join")
    suspend fun joinColocation(@Body body: JoinColocationRequest): AuthResponse

    @PATCH("colocations/{id}")
    suspend fun updateColocation(
        @Path("id") id: String,
        @Body body: UpdateColocationRequest
    ): ColocationResponse

    @POST("colocations/{colocationId}/members")
    suspend fun addMember(
        @Path("colocationId") colocationId: String,
        @Body body: AddMemberRequest
    ): CreateUserResponse

    @DELETE("colocations/{colocationId}/members/{userId}")
    suspend fun removeMember(
        @Path("colocationId") colocationId: String,
        @Path("userId") userId: String
    )

    @PUT("colocations/{colocationId}/purchase-order")
    suspend fun setPurchaseOrder(
        @Path("colocationId") colocationId: String,
        @Body body: SetPurchaseOrderRequest
    ): ColocationResponse

    @PATCH("colocations/{colocationId}/members/{userId}/toggle-active")
    suspend fun toggleMemberActive(
        @Path("colocationId") colocationId: String,
        @Path("userId") userId: String
    )

    // -- Shopping --

    @GET("shopping/{colocationId}")
    suspend fun getShoppingList(
        @Path("colocationId") colocationId: String
    ): List<ShoppingItemResponse>

    @POST("shopping")
    suspend fun createShoppingItem(@Body body: CreateShoppingItemRequest): ShoppingItemResponse

    @PATCH("shopping/{id}/toggle")
    suspend fun toggleShoppingItem(@Path("id") id: String): ShoppingItemResponse

    @DELETE("shopping/{id}")
    suspend fun deleteShoppingItem(@Path("id") id: String)

    // -- Storage --

    @POST("storage/signed-upload-url")
    suspend fun getSignedUploadUrl(@Body body: SignedUploadUrlRequest): SignedUploadUrlResponse

    // -- Receipts --

    @POST("receipts")
    suspend fun createReceipt(@Body body: CreateReceiptRequest): ReceiptResponse

    @GET("receipts/{colocationId}")
    suspend fun getReceipts(
        @Path("colocationId") colocationId: String
    ): List<ReceiptResponse>

    @GET("receipts/{colocationId}/stats")
    suspend fun getExpenseStats(
        @Path("colocationId") colocationId: String
    ): ExpenseStatsResponse

    @DELETE("receipts/{id}")
    suspend fun deleteReceipt(@Path("id") id: String)

    @GET("receipts/{colocationId}/articles")
    suspend fun getArticleCatalog(
        @Path("colocationId") colocationId: String
    ): List<ArticleSuggestion>

    @GET("receipts/{colocationId}/articles/stats")
    suspend fun getArticleStats(
        @Path("colocationId") colocationId: String
    ): List<ArticleStat>

    // -- Catalog --

    @GET("catalog/{colocationId}")
    suspend fun getCatalogArticles(
        @Path("colocationId") colocationId: String
    ): List<CatalogArticle>

    @GET("catalog/{colocationId}/categories")
    suspend fun getCatalogCategories(
        @Path("colocationId") colocationId: String
    ): List<String>

    @POST("catalog")
    suspend fun createCatalogArticle(@Body body: CreateCatalogArticleRequest): CatalogArticle

    @DELETE("catalog/{id}")
    suspend fun deleteCatalogArticle(@Path("id") id: String)

    // -- Rotation --

    @GET("rotations/{colocationId}")
    suspend fun getRotation(
        @Path("colocationId") colocationId: String
    ): List<RotationEntryResponse>

    @POST("rotations/{colocationId}/generate")
    suspend fun generateRotation(
        @Path("colocationId") colocationId: String
    ): List<RotationEntryResponse>

    @PATCH("rotations/swap")
    suspend fun swapRotation(@Body body: SwapRotationRequest): List<RotationEntryResponse>

    // -- Device --

    @POST("notifications/devices")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest)

    @DELETE("notifications/devices/{token}")
    suspend fun unregisterDevice(@Path("token") token: String)

    // -- Notifications --

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead()

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String)

    // -- Reports --

    @GET("reports/{colocationId}")
    suspend fun getReports(
        @Path("colocationId") colocationId: String,
        @Query("tag") tag: String? = null
    ): List<ReportResponse>

    @POST("reports")
    suspend fun createReport(@Body body: CreateReportRequest): ReportResponse

    @GET("reports/{id}/detail")
    suspend fun getReportDetail(@Path("id") id: String): ReportDetailResponse

    @POST("reports/{id}/comments")
    suspend fun createReportComment(
        @Path("id") id: String,
        @Body body: CreateReportCommentRequest
    ): ReportCommentResponse

    @PATCH("reports/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Body body: UpdateReportRequest
    ): ReportDetailResponse

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: String)

    @GET("reports/tags/{colocationId}")
    suspend fun getReportTags(
        @Path("colocationId") colocationId: String
    ): List<ReportTagResponse>

    @POST("reports/tags")
    suspend fun createReportTag(@Body body: CreateTagRequest): ReportTagResponse

    @DELETE("reports/tags/{id}")
    suspend fun deleteReportTag(@Path("id") id: String)

    // -- Menage --

    @GET("menage/{colocationId}")
    suspend fun getMenageWeek(
        @Path("colocationId") colocationId: String
    ): MenageWeekResponse

    @POST("menage/{colocationId}/done")
    suspend fun markMenageDone(
        @Path("colocationId") colocationId: String,
        @Body body: MarkMenageDoneRequest
    )

    @POST("menage/{colocationId}/sub-tasks")
    suspend fun addMenageSubTask(
        @Path("colocationId") colocationId: String,
        @Body body: MenageSubTaskRequest
    )

    @DELETE("menage/{colocationId}/done")
    suspend fun undoMenageDone(
        @Path("colocationId") colocationId: String
    )

    @PATCH("menage/{colocationId}/task-description")
    suspend fun updateMenageTaskDescription(
        @Path("colocationId") colocationId: String,
        @Body body: UpdateTaskDescriptionRequest
    )

    @PATCH("menage/{colocationId}/sub-task-limit")
    suspend fun updateMenageSubTaskLimit(
        @Path("colocationId") colocationId: String,
        @Body body: UpdateSubTaskLimitRequest
    )
}

/** Small request body for device registration. */
data class RegisterDeviceRequest(
    val platform: String,
    val fcmToken: String
)

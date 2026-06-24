package com.habizy.app.data.model

// -- Requests --

data class SignedUploadUrlRequest(
    val folder: String,
    val fileName: String,
    val contentType: String
)

// -- Responses --

data class SignedUploadUrlResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val key: String
)

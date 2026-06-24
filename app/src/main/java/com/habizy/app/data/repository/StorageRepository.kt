package com.habizy.app.data.repository

import com.habizy.app.data.model.SignedUploadUrlRequest
import com.habizy.app.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class StorageRepository(private val api: ApiService) {

    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads image bytes to GCS via a signed URL and returns the public URL.
     *
     * @param imageBytes JPEG-compressed image data
     * @param folder storage folder (e.g. "receipts", "reports")
     * @param contentType MIME type, defaults to "image/jpeg"
     * @return public URL of the uploaded image
     */
    suspend fun uploadImage(
        imageBytes: ByteArray,
        folder: String = "receipts",
        contentType: String = "image/jpeg"
    ): Result<String> = runCatching {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"

        // 1. Get signed upload URL from our API
        val signed = api.getSignedUploadUrl(
            SignedUploadUrlRequest(folder, fileName, contentType)
        )

        // 2. PUT the image bytes to GCS
        val requestBody = imageBytes.toRequestBody(contentType.toMediaType())
        val request = Request.Builder()
            .url(signed.uploadUrl)
            .put(requestBody)
            .header("Content-Type", contentType)
            .build()

        val response = withContext(Dispatchers.IO) {
            uploadClient.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            throw Exception("Upload failed with status ${response.code}")
        }

        signed.publicUrl
    }
}

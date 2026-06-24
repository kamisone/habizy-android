package com.habizy.app.data.remote

import com.habizy.app.BuildConfig
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private lateinit var tokenManager: TokenManager
    private lateinit var _apiService: ApiService

    /** Must be called once at app startup (e.g. in Application.onCreate). */
    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
        _apiService = buildRetrofit().create(ApiService::class.java)
    }

    val apiService: ApiService
        get() = _apiService

    // ---- internals ----

    private fun buildRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Interceptor that attaches the Bearer token to every request
     * and transparently refreshes it on 401.
     */
    private class AuthInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // Skip auth header for public endpoints
            val path = originalRequest.url.encodedPath
            val isPublic = path.endsWith("auth/login") ||
                    path.endsWith("auth/register") ||
                    path.endsWith("auth/refresh") ||
                    path.endsWith("colocations/join")

            val request = if (!isPublic) {
                val token = runBlocking { tokenManager.getAccessToken() }
                if (token != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }
            } else {
                originalRequest
            }

            val response = chain.proceed(request)

            // Handle 401 - try refresh
            if (response.code == 401 && !isPublic) {
                response.close()

                val refreshed = runBlocking { tryRefreshToken() }
                if (refreshed) {
                    val newToken = runBlocking { tokenManager.getAccessToken() }
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(retryRequest)
                } else {
                    // Clear tokens - session expired
                    runBlocking { tokenManager.clear() }
                }
            }

            return response
        }

        private suspend fun tryRefreshToken(): Boolean {
            val refreshToken = tokenManager.getRefreshToken() ?: return false

            return try {
                // Build a plain Retrofit instance without the auth interceptor to avoid recursion
                val plainClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val plainRetrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(plainClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val plainApi = plainRetrofit.create(ApiService::class.java)
                val authResponse = plainApi.refreshToken(
                    RefreshTokenRequest(refreshToken = refreshToken)
                )

                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}

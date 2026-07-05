package com.habizy.app

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.remote.RegisterDeviceRequest
import com.habizy.app.notification.NotificationChannelHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabizyApp : Application() {

    companion object {
        private const val TAG = "HabizyApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the API client with a TokenManager
        ApiClient.init(TokenManager(this))

        // Create the notification channel (Android 8+ requirement)
        NotificationChannelHelper.createChannel(this)

        // Retrieve and persist the current FCM token so AuthViewModel can
        // register it with the server after login.
        val tokenManager = TokenManager(this)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token: $token")
                CoroutineScope(Dispatchers.IO).launch {
                    tokenManager.saveFcmToken(token)
                    // Re-register the device if the user is already logged in.
                    // This handles app re-installs and FCM token rotations where
                    // the user skips the login flow entirely.
                    val accessToken = tokenManager.getAccessToken()
                    if (accessToken != null) {
                        try {
                            ApiClient.apiService.registerDevice(
                                RegisterDeviceRequest(platform = "android", fcmToken = token)
                            )
                        } catch (_: Exception) {
                            // Silently ignore — AuthViewModel.registerFcmDevice() will
                            // retry on the next explicit login.
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to retrieve FCM token", e)
            }
    }
}

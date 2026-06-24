package com.habizy.app

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.notification.NotificationChannelHelper

/**
 * Application class -- entry point that initializes global singletons,
 * mirroring the iOS CagnotteApp / AppDelegate setup.
 */
class CagnotteApp : Application() {

    companion object {
        private const val TAG = "CagnotteApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the API client with a TokenManager
        ApiClient.init(TokenManager(this))

        // Create the notification channel (Android 8+ requirement)
        NotificationChannelHelper.createChannel(this)

        // Retrieve the current FCM token (useful for first install)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token: $token")
                // Token will be registered with the server when the user logs in
                // and CagnotteMessagingService.onNewToken handles refreshes.
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to retrieve FCM token", e)
            }
    }
}

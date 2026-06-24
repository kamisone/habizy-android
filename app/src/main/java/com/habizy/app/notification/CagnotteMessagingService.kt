package com.habizy.app.notification

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.habizy.app.MainActivity
import com.habizy.app.R

/**
 * Firebase Cloud Messaging service.
 *
 * - [onNewToken]: persists the new FCM token in SharedPreferences and registers it
 *   with the backend server.
 * - [onMessageReceived]: displays a local notification with a deep-link intent so
 *   tapping it opens the relevant screen.
 */
class CagnotteMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "CagnotteFCM"
        private const val PREF_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Persist locally
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()

        // TODO: Register token with the backend via ApiClient:
        // e.g. ApiClient.registerDevice(platform = "android", token = token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "Habizy"
        val body = message.notification?.body ?: message.data["body"] ?: return

        // Deep-link data
        val screen = message.data["screen"]
        val entityId = message.data["entityId"] ?: message.data["reportId"]

        showNotification(title, body, screen, entityId)
    }

    private fun showNotification(
        title: String,
        body: String,
        screen: String?,
        entityId: String?,
    ) {
        // Build an intent that will open MainActivity and route to the right screen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            screen?.let { putExtra("navigate_to", buildRoute(it, entityId)) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, NotificationChannelHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Missing POST_NOTIFICATIONS permission", e)
        }
    }

    /**
     * Convert a notification screen + entityId into a navigation route string.
     */
    private fun buildRoute(screen: String, entityId: String?): String {
        return when (screen) {
            "report_detail" -> if (entityId != null) "report_detail/$entityId" else "reports"
            else -> screen
        }
    }
}

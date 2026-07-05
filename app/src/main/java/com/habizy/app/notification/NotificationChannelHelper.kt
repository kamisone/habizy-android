package com.habizy.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * Creates the app's notification channel.
 * Must be called once early in the app lifecycle (from [com.habizy.app.HabizyApp.onCreate]).
 */
object NotificationChannelHelper {

    const val CHANNEL_ID = "habizy_general"
    private const val CHANNEL_NAME = "Notifications Habizy"
    private const val CHANNEL_DESCRIPTION = "Notifications generales de l'application Habizy"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }

        val manager = context.getSystemService<NotificationManager>()
        manager?.createNotificationChannel(channel)
    }
}

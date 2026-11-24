package dev.hiarni.wifidisabler.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.hiarni.wifidisabler.R
import dev.hiarni.wifidisabler.ui.MainActivity

/**
 * All notifications are silent and created lazily – channels only appear once a feature
 * that needs them is actually used. A failed switch-off is always reported, everything else is opt-in.
 */
object Notifier {
    const val CHANNEL_SERVICE = "guard_service"
    const val CHANNEL_EVENTS = "guard_events"
    const val ID_SERVICE = 1
    private const val ID_EVENT = 2

    private fun manager(context: Context) = context.getSystemService(NotificationManager::class.java)

    fun ensureServiceChannel(context: Context) {
        manager(context)?.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SERVICE,
                context.getString(R.string.channel_service_name),
                NotificationManager.IMPORTANCE_MIN,
            ).apply {
                description = context.getString(R.string.channel_service_desc)
                setShowBadge(false)
            },
        )
    }

    private fun ensureEventChannel(context: Context) {
        manager(context)?.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EVENTS,
                context.getString(R.string.channel_events_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.channel_events_desc)
                setShowBadge(false)
            },
        )
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    fun serviceNotification(context: Context): Notification {
        ensureServiceChannel(context)
        return Notification.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_tile)
            .setContentTitle(context.getString(R.string.service_notification_title))
            .setContentIntent(openAppIntent(context))
            .setOngoing(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    fun showDisabled(context: Context, ssid: String, success: Boolean) {
        ensureEventChannel(context)
        val text = if (success) {
            context.getString(R.string.event_notification_success, ssid)
        } else {
            context.getString(R.string.event_notification_failed, ssid)
        }
        val notification = Notification.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_tile)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        runCatching { manager(context)?.notify(ID_EVENT, notification) }
    }
}

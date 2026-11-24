package dev.hiarni.wifidisabler.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import dev.hiarni.wifidisabler.core.Notifier

/**
 * Optional "reliable mode": a foreground service that does nothing but keep the process alive,
 * for systems that kill background apps aggressively. Detection itself still runs through the
 * system-held network callback. Its notification uses the lowest importance (no status bar icon).
 */
class GuardService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(Notifier.ID_SERVICE, Notifier.serviceNotification(this))
        return START_STICKY
    }

    companion object {
        fun start(context: Context) {
            runCatching {
                context.startForegroundService(Intent(context, GuardService::class.java))
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardService::class.java))
        }
    }
}

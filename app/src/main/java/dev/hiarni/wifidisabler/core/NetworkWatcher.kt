package dev.hiarni.wifidisabler.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dev.hiarni.wifidisabler.receiver.NetworkEventReceiver

/**
 * Registers a system-held network callback that fires a broadcast whenever a Wi-Fi network
 * becomes available. No service has to run for this – the system wakes the app on demand.
 * The registration is lost on reboot / app update and is re-armed by [dev.hiarni.wifidisabler.receiver.BootReceiver].
 */
object NetworkWatcher {
    const val ACTION_WIFI_AVAILABLE = "dev.hiarni.wifidisabler.action.WIFI_AVAILABLE"
    private const val REQUEST_CODE = 1

    private fun mutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0

    private fun pendingIntent(context: Context, flags: Int): PendingIntent? {
        val intent = Intent(context, NetworkEventReceiver::class.java).setAction(ACTION_WIFI_AVAILABLE)
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags or mutableFlag())
    }

    /**
     * @param force re-register even if a registration already exists. A fresh registration makes the
     * system report the current network again – wanted after settings changes, not on every process start.
     */
    fun register(context: Context, force: Boolean = true) {
        val app = context.applicationContext
        if (!force && pendingIntent(app, PendingIntent.FLAG_NO_CREATE) != null) return
        val cm = app.getSystemService(ConnectivityManager::class.java) ?: return
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            // Also catch local-only networks without internet (e.g. car or device hotspots).
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val pi = pendingIntent(app, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        // Re-registering the same PendingIntent replaces the previous request – safe to call repeatedly.
        runCatching { cm.registerNetworkCallback(request, pi) }
    }

    fun unregister(context: Context) {
        val app = context.applicationContext
        val cm = app.getSystemService(ConnectivityManager::class.java) ?: return
        val pi = pendingIntent(app, PendingIntent.FLAG_NO_CREATE) ?: return
        runCatching { cm.unregisterNetworkCallback(pi) }
        pi.cancel()
    }
}

package dev.hiarni.wifidisabler.receiver

import android.content.Context
import android.content.Intent
import dev.hiarni.wifidisabler.core.Guard
import dev.hiarni.wifidisabler.core.NetworkWatcher

/** Target of the system-held network callback registered by [NetworkWatcher]. */
class NetworkEventReceiver : AsyncReceiver() {
    override suspend fun handle(context: Context, intent: Intent) {
        if (intent.action != NetworkWatcher.ACTION_WIFI_AVAILABLE) return
        Guard.onWifiAvailable(context)
    }
}

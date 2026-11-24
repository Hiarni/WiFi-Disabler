package dev.hiarni.wifidisabler.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

/** Reads the currently connected Wi-Fi network name. Requires location access + location enabled. */
object WifiReader {
    @Suppress("DEPRECATION")
    fun currentSsid(context: Context): String? {
        val app = context.applicationContext
        if (!isWifiConnected(app)) return null
        val wifi = app.getSystemService(WifiManager::class.java) ?: return null
        return Ssid.normalize(runCatching { wifi.connectionInfo?.ssid }.getOrNull())
    }

    @Suppress("DEPRECATION")
    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        return cm.allNetworks.any { network ->
            cm.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
    }
}

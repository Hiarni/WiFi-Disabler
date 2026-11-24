package dev.hiarni.wifidisabler.core

import android.content.Context
import android.net.wifi.WifiManager

/**
 * Abstraction over "how Wi-Fi gets turned off", so the mechanism can be swapped
 * (e.g. for a Shizuku/root backend) without touching the rest of the app.
 */
fun interface WifiSwitch {
    /** @return `true` if the system accepted the request. */
    fun disable(): Boolean
}

/**
 * Uses the classic [WifiManager.setWifiEnabled] API. Since Android 10 this only works for apps
 * that target API 28 or lower – which is exactly why this app's targetSdk is 28.
 */
class LegacyWifiSwitch(context: Context) : WifiSwitch {
    private val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)

    @Suppress("DEPRECATION")
    override fun disable(): Boolean = runCatching { wifiManager.setWifiEnabled(false) }.getOrDefault(false)
}

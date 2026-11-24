package dev.hiarni.wifidisabler.core

import android.content.Context
import dev.hiarni.wifidisabler.data.SettingsRepository
import dev.hiarni.wifidisabler.service.GuardService
import kotlinx.coroutines.delay

/** Central decision logic: is this a blocked network, and if so, switch Wi-Fi off. */
object Guard {
    private const val SSID_RETRIES = 8
    private const val SSID_RETRY_DELAY_MS = 400L

    /** Called whenever the system reports an available Wi-Fi network. */
    suspend fun onWifiAvailable(context: Context, wifiSwitch: WifiSwitch = LegacyWifiSwitch(context)) {
        val repo = SettingsRepository.get(context)
        val settings = repo.snapshot()
        if (!settings.isActive) return

        // Right after association the SSID can briefly be unavailable – retry for a few seconds.
        var ssid: String? = null
        repeat(SSID_RETRIES) { attempt ->
            ssid = WifiReader.currentSsid(context)
            if (ssid != null) return@repeat
            if (attempt < SSID_RETRIES - 1) delay(SSID_RETRY_DELAY_MS)
        }
        val name = ssid ?: return
        if (!Ssid.isBlocked(name, settings.blocked)) return

        val success = wifiSwitch.disable()
        repo.addEvent(GuardEvent(System.currentTimeMillis(), name, success))
        if (settings.notifyOnDisable || !success) Notifier.showDisabled(context, name, success)
    }

    /**
     * Brings system registrations in line with the stored settings. Idempotent.
     * @param force see [NetworkWatcher.register]; `false` only for plain process starts.
     */
    suspend fun sync(context: Context, force: Boolean = true) {
        val settings = SettingsRepository.get(context).snapshot()
        if (settings.isActive) NetworkWatcher.register(context, force) else NetworkWatcher.unregister(context)
        if (settings.isActive && settings.reliableMode) GuardService.start(context) else GuardService.stop(context)
    }
}

package dev.hiarni.wifidisabler

import android.app.Application
import dev.hiarni.wifidisabler.core.Guard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WifiDisablerApp : Application() {
    /** Process-wide scope for short fire-and-forget work (settings writes, re-registration). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Whenever the process starts, make sure the watcher is armed (without re-triggering it).
        appScope.launch { Guard.sync(this@WifiDisablerApp, force = false) }
    }
}

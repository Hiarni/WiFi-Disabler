package dev.hiarni.wifidisabler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Runs suspending work off the main thread while keeping the broadcast alive via goAsync(). */
abstract class AsyncReceiver : BroadcastReceiver() {
    abstract suspend fun handle(context: Context, intent: Intent)

    final override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        scope.launch {
            try {
                withTimeoutOrNull(TIMEOUT_MS) { handle(context.applicationContext, intent) }
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        /** Well below the system's background broadcast limit. */
        const val TIMEOUT_MS = 8_000L
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

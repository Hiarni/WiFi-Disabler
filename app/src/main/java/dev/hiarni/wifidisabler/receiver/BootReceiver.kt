package dev.hiarni.wifidisabler.receiver

import android.content.Context
import android.content.Intent
import dev.hiarni.wifidisabler.core.Guard

/** Network callback registrations do not survive a reboot or app update – re-arm them. */
class BootReceiver : AsyncReceiver() {
    override suspend fun handle(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> Guard.sync(context)
        }
    }
}

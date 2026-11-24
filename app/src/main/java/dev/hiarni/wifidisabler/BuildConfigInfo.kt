package dev.hiarni.wifidisabler

import android.content.Context

/** Version info without enabling BuildConfig generation. */
object BuildConfigInfo {
    fun versionName(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault("")
}

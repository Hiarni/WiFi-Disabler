package dev.hiarni.wifidisabler.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

/** Snapshot of everything the app needs from the system to work unattended. */
data class SetupState(
    val locationGranted: Boolean,
    val backgroundLocationGranted: Boolean,
    val locationServiceOn: Boolean,
    val batteryUnrestricted: Boolean,
) {
    /** Required for reading the network name in the background. */
    val isReady: Boolean get() = locationGranted && backgroundLocationGranted && locationServiceOn

    companion object {
        fun read(context: Context): SetupState {
            fun granted(p: String) =
                ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

            val fine = granted(Manifest.permission.ACCESS_FINE_LOCATION)
            val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                fine
            }
            val lm = context.getSystemService(LocationManager::class.java)
            val pm = context.getSystemService(PowerManager::class.java)
            return SetupState(
                locationGranted = fine,
                backgroundLocationGranted = fine && background,
                locationServiceOn = lm != null && LocationManagerCompat.isLocationEnabled(lm),
                batteryUnrestricted = pm?.isIgnoringBatteryOptimizations(context.packageName) == true,
            )
        }
    }
}

/** Manufacturers known for aggressively killing background apps (see dontkillmyapp.com). */
object Vendor {
    private val aggressive = setOf(
        "xiaomi", "huawei", "honor", "oppo", "realme", "oneplus", "vivo", "iqoo",
        "samsung", "meizu", "asus", "nothing", "tecno", "infinix", "motorola", "lenovo",
    )

    fun isAggressive(manufacturer: String = Build.MANUFACTURER, brand: String = Build.BRAND): Boolean =
        manufacturer.lowercase() in aggressive || brand.lowercase() in aggressive
}

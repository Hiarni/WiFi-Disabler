package dev.hiarni.wifidisabler.core

import android.Manifest
import android.content.ComponentName
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

/**
 * Manufacturers whose systems have their own "autostart" switch that blocks background apps until
 * allowed (see dontkillmyapp.com). Samsung, Motorola, Nothing, Pixel etc. have no such switch –
 * there the battery setting is all it takes.
 */
object Vendor {
    private val withAutostart = setOf(
        "xiaomi", "redmi", "poco", "huawei", "honor", "oppo", "realme", "oneplus",
        "vivo", "iqoo", "meizu", "tecno", "infinix", "itel",
    )

    fun hasAutostart(manufacturer: String = Build.MANUFACTURER, brand: String = Build.BRAND): Boolean =
        manufacturer.lowercase() in withAutostart || brand.lowercase() in withAutostart

    /**
     * Known autostart pages, tried in order. Not public API – they differ between system versions,
     * so callers fall back to the app info page if none of them opens.
     */
    val autostartPages: List<ComponentName> = listOf(
        "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
        "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
        "com.hihonor.systemmanager" to "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
        "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
        "com.oplus.safecenter" to "com.oplus.safecenter.permission.startup.StartupAppListActivity",
        "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
        "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
        "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
        "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager",
        "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
        "com.meizu.safe" to "com.meizu.safe.permission.SmartBGActivity",
        "com.transsion.phonemaster" to "com.cyin.himgr.autostart.AutoStartActivity",
    ).map { (pkg, cls) -> ComponentName(pkg, cls) }
}

package dev.hiarni.wifidisabler.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.hiarni.wifidisabler.core.Notifier
import dev.hiarni.wifidisabler.core.Vendor
import dev.hiarni.wifidisabler.ui.theme.WifiDisablerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val locationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.refresh()
            // Android 11+ only grants "Allow all the time" from the settings page.
            val setup = viewModel.state.value.setup
            if (setup.locationGranted && !setup.backgroundLocationGranted &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            ) {
                openAppSettings()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WifiDisablerTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
                MainScreen(
                    state = state,
                    actions = MainActions(
                        onEnabledChange = viewModel::setEnabled,
                        onReliableModeChange = viewModel::setReliableMode,
                        onNotifyChange = viewModel::setNotifyOnDisable,
                        onAddNetwork = viewModel::addNetwork,
                        onRemoveNetwork = viewModel::removeNetwork,
                        onClearHistory = viewModel::clearEvents,
                        onGrantLocation = ::requestLocation,
                        onEnableLocationService = {
                            launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        },
                        onBatteryUnrestricted = ::requestBatteryExemption,
                        onOpenAutostart = ::openAutostart,
                        onHideServiceNotification = ::openServiceNotificationSettings,
                        onDismissReliability = viewModel::dismissReliabilityHint,
                        onOpenSource = { launch(Intent(Intent.ACTION_VIEW, SOURCE_URL.toUri())) },
                    ),
                )
            }
        }
    }

    private fun requestLocation() {
        val setup = viewModel.state.value.setup
        when {
            !setup.locationGranted -> locationLauncher.launch(
                buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    // Android 10 still offers "Allow all the time" in the regular dialog.
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }.toTypedArray(),
            )
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ->
                locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            else -> openAppSettings()
        }
    }

    @SuppressLint("BatteryLife") // Sideloaded utility whose core job requires running unattended.
    private fun requestBatteryExemption() {
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName".toUri())
        if (!launch(direct)) launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    private fun openAppSettings() {
        launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
    }

    /** Opens the manufacturer's autostart page if one is known, otherwise the app info page. */
    private fun openAutostart() {
        val opened = Vendor.autostartPages.any { page ->
            launch(Intent().setComponent(page).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        if (!opened) openAppSettings()
    }

    /**
     * Android requires a notification for the reliable-mode service, but the user may switch its
     * channel off – the service keeps running. Opens exactly that channel's settings.
     */
    private fun openServiceNotificationSettings() {
        Notifier.ensureServiceChannel(this)
        val channel = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, Notifier.CHANNEL_SERVICE)
        if (!launch(channel)) {
            launch(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
        }
    }

    private fun launch(intent: Intent): Boolean = try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

    private companion object {
        const val SOURCE_URL = "https://github.com/Hiarni/WiFi-Disabler"
    }
}

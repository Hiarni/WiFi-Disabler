package dev.hiarni.wifidisabler.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hiarni.wifidisabler.BuildConfigInfo
import dev.hiarni.wifidisabler.core.Guard
import dev.hiarni.wifidisabler.core.SetupState
import dev.hiarni.wifidisabler.core.Ssid
import dev.hiarni.wifidisabler.core.Vendor
import dev.hiarni.wifidisabler.core.WifiReader
import dev.hiarni.wifidisabler.data.Settings
import dev.hiarni.wifidisabler.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val settings: Settings = Settings(),
    val setup: SetupState = SetupState(true, true, true, true),
    val currentSsid: String? = null,
    val hasAutostart: Boolean = false,
    val versionName: String = "",
    val loaded: Boolean = false,
) {
    val showReliabilityCard: Boolean
        get() = settings.isActive && !settings.reliabilityHintDismissed &&
            (!setup.batteryUnrestricted || hasAutostart)
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository.get(app)
    private val setup = MutableStateFlow(SetupState.read(app))
    private val currentSsid = MutableStateFlow<String?>(null)

    val state: StateFlow<UiState> = combine(repo.settings, setup, currentSsid) { s, setupState, ssid ->
        UiState(
            settings = s,
            setup = setupState,
            currentSsid = ssid,
            hasAutostart = Vendor.hasAutostart(),
            versionName = BuildConfigInfo.versionName(app),
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    /** Re-reads system state (permissions, location toggle, current network). Called on resume. */
    fun refresh() {
        val app = getApplication<Application>()
        setup.value = SetupState.read(app)
        viewModelScope.launch(Dispatchers.Default) { currentSsid.value = WifiReader.currentSsid(app) }
    }

    private fun mutate(block: suspend SettingsRepository.() -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            repo.block()
            Guard.sync(getApplication())
        }
    }

    fun setEnabled(value: Boolean) = mutate { setEnabled(value) }
    fun setReliableMode(value: Boolean) = mutate { setReliableMode(value) }
    fun setNotifyOnDisable(value: Boolean) = mutate { setNotifyOnDisable(value) }
    fun dismissReliabilityHint() = mutate { dismissReliabilityHint() }
    fun removeNetwork(ssid: String) = mutate { removeBlocked(ssid) }
    fun clearEvents() = mutate { clearEvents() }

    /** @return false if the input was not a valid network name. */
    fun addNetwork(input: String): Boolean {
        val ssid = Ssid.fromUserInput(input) ?: return false
        mutate { addBlocked(ssid) }
        return true
    }
}

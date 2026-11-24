package dev.hiarni.wifidisabler.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.hiarni.wifidisabler.core.GuardEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class Settings(
    val enabled: Boolean = true,
    val blocked: Set<String> = emptySet(),
    val reliableMode: Boolean = false,
    val notifyOnDisable: Boolean = false,
    val reliabilityHintDismissed: Boolean = false,
    val events: List<GuardEvent> = emptyList(),
) {
    /** The watcher only needs to run when it is switched on and has something to watch for. */
    val isActive: Boolean get() = enabled && blocked.isNotEmpty()
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.dataStore

    private object Keys {
        val enabled = booleanPreferencesKey("enabled")
        val blocked = stringSetPreferencesKey("blocked_ssids")
        val reliable = booleanPreferencesKey("reliable_mode")
        val notify = booleanPreferencesKey("notify_on_disable")
        val hintDismissed = booleanPreferencesKey("reliability_hint_dismissed")
        val events = stringPreferencesKey("events")
    }

    val settings: Flow<Settings> = store.data.map { p ->
        Settings(
            enabled = p[Keys.enabled] ?: true,
            blocked = p[Keys.blocked] ?: emptySet(),
            reliableMode = p[Keys.reliable] ?: false,
            notifyOnDisable = p[Keys.notify] ?: false,
            reliabilityHintDismissed = p[Keys.hintDismissed] ?: false,
            events = GuardEvent.decode(p[Keys.events]),
        )
    }

    suspend fun snapshot(): Settings = settings.first()

    suspend fun setEnabled(value: Boolean) = store.edit { it[Keys.enabled] = value }
    suspend fun setReliableMode(value: Boolean) = store.edit { it[Keys.reliable] = value }
    suspend fun setNotifyOnDisable(value: Boolean) = store.edit { it[Keys.notify] = value }
    suspend fun dismissReliabilityHint() = store.edit { it[Keys.hintDismissed] = true }

    suspend fun addBlocked(ssid: String) = store.edit {
        it[Keys.blocked] = (it[Keys.blocked] ?: emptySet()) + ssid
    }

    suspend fun removeBlocked(ssid: String) = store.edit {
        it[Keys.blocked] = (it[Keys.blocked] ?: emptySet()) - ssid
    }

    suspend fun addEvent(event: GuardEvent) = store.edit {
        it[Keys.events] = GuardEvent.encode(GuardEvent.prepend(event, GuardEvent.decode(it[Keys.events])))
    }

    suspend fun clearEvents() = store.edit { it.remove(Keys.events) }

    companion object {
        @Volatile private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}

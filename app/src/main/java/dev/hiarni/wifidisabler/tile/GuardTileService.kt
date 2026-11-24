package dev.hiarni.wifidisabler.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.hiarni.wifidisabler.R
import dev.hiarni.wifidisabler.core.Guard
import dev.hiarni.wifidisabler.data.Settings
import dev.hiarni.wifidisabler.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Quick Settings tile to pause/resume the guard without opening the app. */
class GuardTileService : TileService() {
    private val scope = newScope()

    private fun newScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartListening() {
        super.onStartListening()
        scope.launch { render(SettingsRepository.get(this@GuardTileService).snapshot()) }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val repo = SettingsRepository.get(this@GuardTileService)
            repo.setEnabled(!repo.snapshot().enabled)
            Guard.sync(this@GuardTileService)
            render(repo.snapshot())
        }
    }

    private suspend fun render(settings: Settings) = withContext(Dispatchers.Main) {
        val tile = qsTile ?: return@withContext
        tile.state = if (settings.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = when {
                settings.blocked.isEmpty() -> getString(R.string.tile_no_networks)
                settings.enabled -> resources.getQuantityString(
                    R.plurals.networks_count, settings.blocked.size, settings.blocked.size,
                )
                else -> getString(R.string.status_paused)
            }
        }
        tile.updateTile()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

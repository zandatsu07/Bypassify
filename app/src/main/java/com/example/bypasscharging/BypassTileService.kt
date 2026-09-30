package com.example.bypasscharging

import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlin.concurrent.thread

class BypassTileService : TileService() {

    private val main = Handler(Looper.getMainLooper())

    override fun onStartListening() {
        super.onStartListening()
        thread {
            val s = BypassController.read()
            main.post { render(s) }
        }
    }

    override fun onClick() {
        super.onClick()
        // Optimistic flip so the tile feels instant, then confirm with the real value.
        val target = qsTile?.state != Tile.STATE_ACTIVE
        qsTile?.apply { state = if (target) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE; updateTile() }
        thread {
            val s = BypassController.write(target)
            main.post { render(s) }
        }
    }

    private fun render(s: BypassController.Status) {
        val tile = qsTile ?: return
        tile.label = getString(R.string.tile_label)
        tile.state = when {
            s.error != null -> Tile.STATE_UNAVAILABLE
            s.enabled == true -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.subtitle = when {
            s.error != null -> "No root"
            s.enabled == true -> "On"
            else -> "Off"
        }
        tile.updateTile()
    }
}

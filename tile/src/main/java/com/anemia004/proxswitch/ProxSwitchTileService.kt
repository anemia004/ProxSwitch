package com.anemia004.proxswitch

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.io.File

class ProxSwitchTileService : TileService() {

    companion object {
        private const val STATE_FILE = "/data/local/proxswitch_state"
    }

    override fun onTileAdded() {
        super.onTileAdded()
        refreshTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        if (isSecure) {
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        Thread {
            val currentlyDisabled = readState()
            val newState = !currentlyDisabled
            writeState(newState)
            refreshTile()
        }.start()
    }

    private fun readState(): Boolean {
        val f = File(STATE_FILE)
        return f.exists() && f.readText().trim() == "1"
    }

    private fun writeState(disabled: Boolean) {
        val value = if (disabled) "1" else "0"
        execRoot("echo $value > $STATE_FILE")
        execRoot("chmod 644 $STATE_FILE")
    }

    private fun refreshTile() {
        val disabled = readState()
        tile.state = if (disabled) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        tile.label = if (disabled) "Prox OFF" else "Prox ON"
        tile.updateTile()
    }

    private fun execRoot(cmd: String) {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            p.waitFor()
        } catch (_: Exception) {}
    }
}

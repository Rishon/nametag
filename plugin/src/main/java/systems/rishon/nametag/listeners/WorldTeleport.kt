package systems.rishon.nametag.listeners

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import systems.rishon.nametag.handler.MainHandler

class WorldTeleport(private val handler: MainHandler) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        val fromWorld = event.from
        val newWorld = player.world
        if (fromWorld.name == newWorld.name) return

        if (player.gameMode == GameMode.SPECTATOR) return

        val nametagEntity = this.handler.nametagData.getPlayerNametag(player.uniqueId) ?: return

        nametagEntity.destroyForAll()

        newWorld.players.forEach {
            nametagEntity.spread(it)
        }
    }
}
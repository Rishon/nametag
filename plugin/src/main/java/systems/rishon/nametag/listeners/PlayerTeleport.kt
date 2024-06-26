package systems.rishon.nametag.listeners

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import systems.rishon.nametag.handler.MainHandler

class PlayerTeleport(private val handler: MainHandler) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if (player.gameMode == GameMode.SPECTATOR) return
        val fromWorld = event.from.world ?: return
        val toWorld = event.to.world ?: return
        val cause = event.cause

        val nametagEntity = this.handler.nametagData.getPlayerNametag(player.uniqueId) ?: return

        if (fromWorld.name == toWorld.name) {
            nametagEntity.updatePosition()
            nametagEntity.setPassengers(fromWorld)
            return
        }

        if (cause == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            nametagEntity.destroyForAll()

            toWorld.players.forEach {
                nametagEntity.spread(it)
            }
        }
    }
}
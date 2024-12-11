package systems.rishon.nametag.listeners

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import systems.rishon.nametag.handler.MainHandler

class PlayerDeath(private val handler: MainHandler) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val nametagEntity = this.handler.nametagData.getPlayerNametag(player.uniqueId) ?: return
        nametagEntity.destroyForAll()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerRespawn(event: PlayerPostRespawnEvent) {
        val player = event.player
        val nametagEntity = this.handler.nametagData.getPlayerNametag(player.uniqueId) ?: return
        val respawnedWorld = event.respawnedLocation.world ?: return

        respawnedWorld.players.forEach {
            nametagEntity.spread(it)
        }
    }

}
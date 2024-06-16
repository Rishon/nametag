package systems.rishon.nametag.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import systems.rishon.nametag.Nametag
import systems.rishon.nametag.entity.NametagEntity
import systems.rishon.nametag.handler.MainHandler

class Connections(private val handler: MainHandler) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerConnected(event: PlayerJoinEvent) {
        val player = event.player

        Nametag.schedulerUtil.runTaskAsync {
            val nameTagEntity = NametagEntity(player)
            nameTagEntity.spawn()
            this.handler.nametagData.addPlayerNametag(player, nameTagEntity)

            // Spread nametag to all players in the world
            player.world.players.forEach { worldPlayer ->
                if (player.uniqueId == worldPlayer.uniqueId) return@forEach
                nameTagEntity.spread(worldPlayer)
                val worldPlayerNameTagEntity = this.handler.nametagData.getPlayerNametag(worldPlayer.uniqueId)
                if (worldPlayerNameTagEntity == null) return@forEach
                worldPlayerNameTagEntity.spread(player)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerDisconnected(event: PlayerQuitEvent) {
        val player = event.player

        Nametag.schedulerUtil.runTaskAsync {
            val nameTagEntity = this.handler.nametagData.getPlayerNametag(player.uniqueId)
            if (nameTagEntity == null) return@runTaskAsync
            nameTagEntity.destroyForAll()
            this.handler.nametagData.removePlayerNametag(player)
        }
    }

}
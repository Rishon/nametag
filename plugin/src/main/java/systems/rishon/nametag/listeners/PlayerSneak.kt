package systems.rishon.nametag.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import systems.rishon.nametag.handler.MainHandler

class PlayerSneak(private val handler: MainHandler) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        val nametagEntity = this.handler.nametagData.getPlayerNametag(player.uniqueId) ?: return
        nametagEntity.handleSneak(event.isSneaking)
    }

}
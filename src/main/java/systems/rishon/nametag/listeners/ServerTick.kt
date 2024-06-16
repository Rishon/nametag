package systems.rishon.nametag.listeners

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ServerTick : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTick(event: ServerTickEndEvent) {

    }

}
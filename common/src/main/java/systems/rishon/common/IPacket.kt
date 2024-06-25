package systems.rishon.common

import net.minecraft.world.entity.Entity
import org.bukkit.entity.Player

interface IPacket {

    fun ClientAddEntityPacket(player: Player, entity: Entity) {

    }

    fun ClientRemoveEntityPacket(player: Player, entityId: Int) {

    }

    fun sendUpdatePacket(player: Player, entity: Entity) {

    }
}
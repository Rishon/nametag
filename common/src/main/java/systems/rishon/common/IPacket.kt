package systems.rishon.common

import net.minecraft.world.entity.Entity
import org.bukkit.entity.Player

interface IPacket {

    fun ClientAddEntityPacket(player: Player, entity: Entity) {

    }

    fun ClientRemoveEntityPacket(player: Player, entityId: Int) {

    }

    fun ClientSetPassengersPacket(player: Player, entity: Entity, passenger: Entity) {

    }

    fun sendUpdatePacket(player: Player, entity: Entity) {

    }
}
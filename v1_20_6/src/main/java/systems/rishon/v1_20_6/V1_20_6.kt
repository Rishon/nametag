package systems.rishon.v1_21

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import systems.rishon.common.IPacket

class V1_20_6 : IPacket {

    override fun ClientAddEntityPacket(player: Player, entity: Entity) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection
        connection.send(ClientboundAddEntityPacket(entity, entity.id, entity.blockPosition()))
    }

    override fun ClientRemoveEntityPacket(player: Player, entityId: Int) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection
        connection.send(ClientboundRemoveEntitiesPacket(entityId))
    }

    override fun sendUpdatePacket(player: Player, entity: Entity) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val metas: List<SynchedEntityData.DataValue<*>?>? = entity.entityData.nonDefaultValues
        if (metas != null && !metas.isEmpty()) serverPlayer.connection.send(
            ClientboundSetEntityDataPacket(
                entity.id, metas
            )
        )
    }

}

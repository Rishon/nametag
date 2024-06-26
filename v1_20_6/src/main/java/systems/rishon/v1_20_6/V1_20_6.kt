package systems.rishon.v1_21

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
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

    override fun ClientSetPassengersPacket(player: Player, entity: Entity, passenger: Entity) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection

        // Reflection to access the private constructor
        try {
            val constructor =
                ClientboundSetPassengersPacket::class.java.getDeclaredConstructor(FriendlyByteBuf::class.java)
            constructor.isAccessible = true
            val buf: ByteBuf = ByteBufAllocator.DEFAULT.buffer(55)
            val friendlyByteBuf = FriendlyByteBuf(buf)
            friendlyByteBuf.writeVarInt(entity.id)
            friendlyByteBuf.writeVarIntArray(intArrayOf(passenger.id))
            connection.send(constructor.newInstance(friendlyByteBuf))
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

package systems.rishon.nametag.entity

import com.mojang.math.Transformation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.joml.Quaternionf
import org.joml.Vector3f

class NametagEntity(private val player: Player) {

    // Passengers
    private val passengers: MutableList<Display.TextDisplay> = mutableListOf()

    init {

    }

    private fun create() {
        val entity = createEntity()
        passengers.add(entity)
    }

    private fun createEntity(): Display.TextDisplay {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val craftWorld: CraftWorld = player.world as CraftWorld
        val serverLevel = craftWorld.handle
        val passenger: Display.TextDisplay = Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel)
        val bukkitPassenger = passenger.bukkitEntity as TextDisplay
        val json = JSONComponentSerializer.json().serialize(Component.text("Hello, world!"))
        val chatComponent = CraftChatMessage.fromJSON(json);

        // NMS
        passenger.moveTo(serverPlayer.position())
        passenger.isNoGravity = true
        passenger.text = chatComponent
        passenger.billboardConstraints = Display.BillboardConstraints.CENTER
        passenger.setTransformation(
            Transformation(
                Vector3f(0.0f, 0.5f, 0.0f), // Translation
                Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), // Left rotation
                Vector3f(1.0f, 1.0f, 1.0f), // Scale
                Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), // Right rotation
            )
        )

        // Bukkit
        bukkitPassenger.alignment = TextDisplay.TextAlignment.CENTER
        return passenger
    }

    fun spawn() {
        create()
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection

        passengers.forEach { passenger ->
            addPassenger(passenger)
            connection.send(ClientboundAddEntityPacket(passenger))
            sendUpdatePacket(player, passenger)
        }
    }

    private fun addPassenger(entity: Entity) {
        val bukkitEntity = entity.bukkitEntity
        this.player.addPassenger(bukkitEntity)
    }

    fun spread(player: Player) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection

        passengers.forEach { passenger ->
            connection.send(ClientboundAddEntityPacket(passenger))
            sendUpdatePacket(player, passenger)
        }
    }

    fun destroyForPlayer() {

    }

    fun destroyForAll() {
        this.player.server.onlinePlayers.forEach { player ->
            this.passengers.forEach { passenger ->
                val craftPlayer = player as CraftPlayer
                val serverPlayer = craftPlayer.handle
                serverPlayer.connection.send(ClientboundRemoveEntitiesPacket(passenger.id))
            }
        }
    }

    private fun sendUpdatePacket(player: Player, entity: Entity) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val metas: List<SynchedEntityData.DataValue<*>?>? = getDataWatcher(entity).nonDefaultValues
        if (metas != null && !metas.isEmpty()) serverPlayer.connection.send(
            ClientboundSetEntityDataPacket(
                entity.id, metas
            )
        )
    }

    private fun getDataWatcher(entity: Entity): SynchedEntityData {
        return entity.entityData
    }

    class Data(private val height: Float) {
    }
}
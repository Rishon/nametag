package systems.rishon.nametag.entity

import com.mojang.math.Transformation
import me.clip.placeholderapi.PlaceholderAPI
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
import systems.rishon.api.paper.color.ColorUtil
import systems.rishon.nametag.handler.FileHandler

class NametagEntity(private val player: Player) {

    // Passengers
    private val passengers: MutableList<Display.TextDisplay> = mutableListOf()

    // FileHandler
    private val fileHandler = FileHandler.handler

    fun spawn() {
        create()
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection

        this.passengers.forEach { passenger ->
            connection.send(ClientboundAddEntityPacket(passenger))
            sendUpdatePacket(player, passenger)
        }

        setPassengers()
    }

    fun spread(player: Player, selfNametag: Boolean = false) {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val connection = serverPlayer.connection

        this.passengers.forEach { passenger ->
            connection.send(ClientboundAddEntityPacket(passenger))
            sendUpdatePacket(player, passenger)
        }
    }

    fun destroyForPlayer() {
        this.passengers.forEach { passenger ->
            val craftPlayer = player as CraftPlayer
            val serverPlayer = craftPlayer.handle
            serverPlayer.connection.send(ClientboundRemoveEntitiesPacket(passenger.id))
        }
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

    fun handleSneak(isSneaking: Boolean) {
        val worldPlayers = this.player.world.players
        val textOpacityBytes: Byte = if (isSneaking) -120 else 0

        this.passengers.forEach { passenger ->
            worldPlayers.forEach { worldPlayer ->
                passenger.textOpacity = textOpacityBytes
                sendUpdatePacket(worldPlayer, passenger)
            }
        }
    }

    private fun create() {
        val lines = this.fileHandler.nametagsFormat

        lines.forEach { text ->
            val entity =
                createEntity(PlaceholderAPI.setPlaceholders(player, text.replace("{player_name}", player.name)))
            this.passengers.add(entity)
        }
    }

    private fun createEntity(text: String): Display.TextDisplay {
        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle
        val craftWorld: CraftWorld = player.world as CraftWorld
        val serverLevel = craftWorld.handle
        val passenger: Display.TextDisplay = Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel)
        val bukkitPassenger = passenger.bukkitEntity as TextDisplay
        val json = JSONComponentSerializer.json().serialize(ColorUtil.translate(text))
        val chatComponent = CraftChatMessage.fromJSON(json);

        // NMS
        passenger.moveTo(serverPlayer.position())
        passenger.isNoGravity = true
        passenger.text = chatComponent
        passenger.billboardConstraints = Display.BillboardConstraints.CENTER
        setTransformation(passenger, Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f))

        // Bukkit
        bukkitPassenger.alignment = TextDisplay.TextAlignment.CENTER
        return passenger
    }

    private fun setPassengers() {
        val worldPlayers = this.player.world.players

        // Line height
        val lineHeight = this.fileHandler.nametagsHeight

        this.passengers.forEachIndexed { index, passenger ->
            val y = 0.3 + (lineHeight * index)
            setTransformation(passenger, Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, y.toFloat(), 0.0f))
            player.addPassenger(passenger.bukkitEntity)
            sendUpdatePacket(player, passenger)
            // Update to all players
            worldPlayers.forEach { worldPlayer ->
                if (player.uniqueId == worldPlayer.uniqueId) return@forEach
                sendUpdatePacket(worldPlayer, passenger)
            }
        }
    }

    fun updateTagForPlayer() {
        val lines = this.fileHandler.nametagsFormat
        val worldPlayers = this.player.world.players

        this.passengers.forEachIndexed { index, passenger ->
            passenger.text = CraftChatMessage.fromJSON(
                JSONComponentSerializer.json().serialize(
                    ColorUtil.translate(
                        PlaceholderAPI.setPlaceholders(
                            player, lines[index].replace("{player_name}", player.name)
                        )
                    )
                )
            )
            worldPlayers.forEach { worldPlayer ->
                sendUpdatePacket(worldPlayer, passenger)
            }
        }
    }

    private fun setTransformation(passenger: Display.TextDisplay, scale: Vector3f, translation: Vector3f) {
        passenger.setTransformation(
            Transformation(
                translation, // Translation
                Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), // Left rotation
                scale, // Scale
                Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), // Right rotation
            )
        )
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
}
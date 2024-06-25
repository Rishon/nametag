package systems.rishon.nametag.entity

import com.mojang.math.Transformation
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.joml.Quaternionf
import org.joml.Vector3f
import systems.rishon.api.paper.color.ColorUtil
import systems.rishon.nametag.Nametag
import systems.rishon.nametag.handler.FileHandler

class NametagEntity(private val player: Player) {

    // Passengers
    private val passengers: MutableList<Display.TextDisplay> = mutableListOf()

    // FileHandler
    private val fileHandler = FileHandler.handler

    fun spawn(shouldCreate: Boolean) {
        if (shouldCreate) create()

        this.passengers.forEach { passenger ->
            Nametag.getPacketManager().ClientAddEntityPacket(player, passenger)
            Nametag.getPacketManager().sendUpdatePacket(player, passenger)
        }

        setPassengers()
    }

    fun spread(player: Player, selfNametag: Boolean = false) {
        this.passengers.forEach { passenger ->
            Nametag.getPacketManager().ClientAddEntityPacket(player, passenger)
            Nametag.getPacketManager().sendUpdatePacket(player, passenger)
        }
    }

    fun destroyForAll() {
        this.player.server.onlinePlayers.forEach { player ->
            this.passengers.forEach { passenger ->
                Nametag.getPacketManager().ClientRemoveEntityPacket(player, passenger.id)
            }
        }
    }

    fun destroyForPlayer(player: Player) {
        this.passengers.forEach { passenger ->
            Nametag.getPacketManager().ClientRemoveEntityPacket(player, passenger.id)
        }
    }

    fun updatePosition() {
        val craftPlayer = player as CraftPlayer
        this.passengers.forEach { passenger ->
            passenger.moveTo(craftPlayer.handle.position())
            updateForWorldPlayers(true)
        }
    }

    fun handleSneak(isSneaking: Boolean) {
        val opacityConfigValue = this.fileHandler.textOpacity
        val textOpacityBytes: Byte =
            if (isSneaking) calculateOpacityValue(opacityConfigValue) else calculateOpacityValue(1.0)

        this.passengers.forEach { passenger ->
            passenger.textOpacity = textOpacityBytes
            updateForWorldPlayers(true)
        }
    }

    private fun calculateOpacityValue(opacity: Double): Byte {
        val clampedOpacity = opacity.coerceIn(0.0, 1.0)
        return (128 - (clampedOpacity * 128).toInt()).toByte()
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
        bukkitPassenger.lineWidth = 200
        return passenger
    }

    fun setPassengers() {
        // Reset passengers
        player.passengers.clear()
        // Line height
        val lineHeight = this.fileHandler.nametagsHeight

        this.passengers.forEachIndexed { index, passenger ->
            val y = 0.3 + (lineHeight * index)
            setTransformation(passenger, Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, y.toFloat(), 0.0f))
            player.addPassenger(passenger.bukkitEntity)
            // Update to all players
            updateForWorldPlayers(true)
        }
    }

    fun updateTagForPlayer() {
        val lines = this.fileHandler.nametagsFormat

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
            updateForWorldPlayers(true)
        }
    }

    private fun updateForWorldPlayers(includeSelf: Boolean) {
        val worldPlayers = this.player.world.players

        this.passengers.forEach { passenger ->
            worldPlayers.forEach { worldPlayer ->
                if (!includeSelf && player.uniqueId == worldPlayer.uniqueId) return@forEach
                Nametag.getPacketManager().sendUpdatePacket(worldPlayer, passenger)
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
}
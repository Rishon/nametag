package systems.rishon.nametag.entity

import com.mojang.math.Transformation
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.GameMode
import org.bukkit.World
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
import systems.rishon.nametag.handler.MainHandler
import java.util.UUID
import java.util.concurrent.CompletableFuture

class NametagEntity(private val player: Player) {

    // Passengers
    private val passengers: MutableList<Display.TextDisplay> = mutableListOf()

    // FileHandler
    private val fileHandler = FileHandler.handler

    // Data
    private val canSee: MutableSet<UUID> = mutableSetOf()

    fun spawn(shouldCreate: Boolean) {
        if (shouldCreate) create()

        this.passengers.forEach { passenger ->
            Nametag.getPacketManager().ClientAddEntityPacket(player, passenger)
            Nametag.getPacketManager().sendUpdatePacket(player, passenger)
        }

        setPassengers(player.world)
    }

    fun spread(player: Player, selfNametag: Boolean = false) {
        this.passengers.forEach { passenger ->
            Nametag.getPacketManager().ClientAddEntityPacket(player, passenger)
            Nametag.getPacketManager().sendUpdatePacket(player, passenger)
        }
        setPassengers(player.world)
        this.canSee.add(player.uniqueId)
    }

    fun destroyForAll() {
        this.player.server.onlinePlayers.forEach { player ->
            this.passengers.forEach { passenger ->
                Nametag.getPacketManager().ClientRemoveEntityPacket(player, passenger.id)
            }
            this.canSee.remove(player.uniqueId)
        }
    }

    fun destroyForPlayer(player: Player) {
        this.passengers.forEach { passenger ->
            Nametag.getPacketManager().ClientRemoveEntityPacket(player, passenger.id)
        }
    }

    fun updatePosition() {
        this.passengers.forEach { passenger ->
            passenger.moveTo((player as CraftPlayer).handle.position())
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
            val entity = createEntity(
                formatName(
                    player, applyPlaceholders(text, player)
                )
            )

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
        passenger.moveTo(serverPlayer.position().add(0.0, -50.0, 0.0))
        passenger.isNoGravity = true
        passenger.text = chatComponent
        passenger.billboardConstraints = Display.BillboardConstraints.CENTER
        setTransformation(passenger, Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f))

        // Bukkit
        bukkitPassenger.alignment = TextDisplay.TextAlignment.CENTER
        bukkitPassenger.lineWidth = 200
        bukkitPassenger.isSeeThrough = true
        return passenger
    }

    fun setPassengers(world: World) {
        if (this.player.gameMode == GameMode.SPECTATOR) return
        val serverPlayer = (this.player as CraftPlayer).handle

        // Line height
        val lineHeight = this.fileHandler.nametagsHeight

        world.players.forEach { worldPlayer ->
            var previousPassenger: Display.TextDisplay? = null
            this.passengers.forEachIndexed { i, pass ->
                val y = 0.3 + (lineHeight * i)
                setTransformation(pass, Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, y.toFloat(), 0.0f))

                if (previousPassenger != null) Nametag.getPacketManager()
                    .ClientSetPassengersPacket(worldPlayer, pass, previousPassenger)
                previousPassenger = pass
            }

            if (previousPassenger != null) Nametag.getPacketManager()
                .ClientSetPassengersPacket(worldPlayer, serverPlayer, previousPassenger)
        }

        updateForWorldPlayers(true)
    }


    fun updateTagForPlayer() {
        val lines = this.fileHandler.nametagsFormat

        this.passengers.forEachIndexed { index, passenger ->
            passenger.text = CraftChatMessage.fromJSON(
                JSONComponentSerializer.json().serialize(
                    ColorUtil.translate(
                        formatName(
                            player, applyPlaceholders(lines[index], player)
                        )
                    )
                )
            )
            updateForWorldPlayers(true)
        }
    }

    private fun updateForWorldPlayers(includeSelf: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val worldPlayers = this.player.world.players

            this.passengers.forEach { passenger ->
                worldPlayers.forEach { worldPlayer ->
                    if (!includeSelf && player.uniqueId == worldPlayer.uniqueId) return@forEach
                    if (worldPlayer.location.distance(player.location) > FileHandler.handler.distanceViewNametags || !worldPlayer.canSee(player) || player.gameMode == GameMode.SPECTATOR) {
                        Nametag.getPacketManager().ClientRemoveEntityPacket(worldPlayer, passenger.id)
                        this.canSee.remove(worldPlayer.uniqueId)
                        return@forEach
                    }

                    if (this.canSee.add(worldPlayer.uniqueId)) spread(worldPlayer)

                    Nametag.getPacketManager().sendUpdatePacket(worldPlayer, passenger)
                }
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

    private fun formatName(player: Player, name: String): String {
        return if (MainHandler.getHandler().placeholderAPI!!) {
            PlaceholderAPI.setPlaceholders(player, name)
        } else {
            name
        }
    }

    private fun applyPlaceholders(string: String, player: Player): String {
        var finalValue = string
        // No need to check for hook
        finalValue = finalValue.replace("{player_name}", player.name)

        // If LuckPerms is hooked
        val luckPermsAPI = MainHandler.getHandler().luckPermsAPI
        if (luckPermsAPI != null) {
            val userData = luckPermsAPI.getUserManager().getUser(player.uniqueId)
            if (userData != null) {
                val cachedMetaData = userData.getCachedData().getMetaData()
                finalValue = finalValue.replace("{luckperms_prefix}", cachedMetaData.prefix!!)
            }
        }
        return finalValue
    }
}
package systems.rishon.nametag.entity

import org.bukkit.entity.Player
import java.util.UUID

class NametagData() {

    // Passengers data
    val playerNameTags = mutableMapOf<UUID, NametagEntity>()

    fun addPlayerNametag(player: Player, nametagEntity: NametagEntity) {
        val uuid = player.uniqueId
        playerNameTags[uuid] = nametagEntity
    }

    fun removePlayerNametag(player: Player) {
        val uuid = player.uniqueId
        playerNameTags.remove(uuid)
    }

    fun getPlayerNametag(uuid: UUID): NametagEntity? {
        return playerNameTags[uuid]
    }

}
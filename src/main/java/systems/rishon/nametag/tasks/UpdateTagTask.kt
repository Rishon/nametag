package systems.rishon.nametag.tasks

import systems.rishon.nametag.handler.MainHandler

class UpdateTagTask(private val handler: MainHandler) : Runnable {

    override fun run() {
        val onlinePlayers = this.handler.plugin.server.onlinePlayers

        onlinePlayers.forEach { player ->
            val uuid = player.uniqueId
            val nametagEntity = this.handler.nametagData.getPlayerNametag(uuid) ?: return@forEach
            nametagEntity.updateTagForPlayer()
        }
    }
}
package systems.rishon.nametag.handler

import org.bukkit.plugin.PluginManager
import systems.rishon.nametag.Nametag
import systems.rishon.nametag.entity.NametagData
import systems.rishon.nametag.entity.NametagEntity
import systems.rishon.nametag.listeners.Connections
import systems.rishon.nametag.listeners.PlayerSneak
import systems.rishon.nametag.utils.LoggerUtil

class MainHandler(val plugin: Nametag) : IHandler {

    // Data
    lateinit var nametagData: NametagData

    override fun init() {
        // Initialize data
        this.nametagData = NametagData()

        // Register listeners
        registerListeners()

        // Load online players
        loadPlayers()
    }

    override fun end() {
        // Unload online players nametags
        this.plugin.server.onlinePlayers.forEach { player ->
            val nameTagEntity = this.nametagData.getPlayerNametag(player.uniqueId)
            if (nameTagEntity == null) return@forEach
            nameTagEntity.destroyForAll()
        }
    }

    private fun registerListeners() {
        LoggerUtil.log("Registering listeners...")
        val pluginManager: PluginManager = this.plugin.server.pluginManager
        pluginManager.registerEvents(Connections(this), this.plugin)
        pluginManager.registerEvents(PlayerSneak(this), this.plugin)
    }

    private fun loadPlayers() {
        Nametag.schedulerUtil.runTaskAsync {
            this.plugin.server.onlinePlayers.forEach { player ->
                val nameTagEntity = NametagEntity(player)
                nameTagEntity.spawn()
                this.nametagData.addPlayerNametag(player, nameTagEntity)

                player.world.players.forEach { worldPlayer ->
                    if (player.uniqueId == worldPlayer.uniqueId) return@forEach
                    nameTagEntity.spread(worldPlayer)
                }
            }
        }
    }
}
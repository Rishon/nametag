package systems.rishon.nametag.handler

import org.bukkit.plugin.PluginManager
import systems.rishon.nametag.Nametag
import systems.rishon.nametag.command.NametagCommand
import systems.rishon.nametag.entity.NametagData
import systems.rishon.nametag.entity.NametagEntity
import systems.rishon.nametag.listeners.Connections
import systems.rishon.nametag.listeners.PlayerDeath
import systems.rishon.nametag.listeners.PlayerSneak
import systems.rishon.nametag.listeners.PlayerTeleport
import systems.rishon.nametag.listeners.WorldTeleport
import systems.rishon.nametag.tasks.UpdateTagTask
import systems.rishon.nametag.utils.LoggerUtil

class MainHandler(val plugin: Nametag) : IHandler {

    // Data
    lateinit var nametagData: NametagData

    // PlaceholderAPI
    var placeholderAPI: Boolean? = null

    override fun init() {
        handler = this

        // Initialize data
        this.nametagData = NametagData()

        // Check if PlaceholderAPI is installed
        this.placeholderAPI = this.plugin.server.pluginManager.getPlugin("PlaceholderAPI") != null

        // Register listeners and commands
        registerListeners()
        registerCommands()

        // Load tasks
        loadTasks()

        // Load online players
        loadPlayers()
    }

    override fun end() {
        // Unload online players nametags
        this.plugin.server.onlinePlayers.forEach { player ->
            val nameTagEntity = this.nametagData.getPlayerNametag(player.uniqueId)
            if (nameTagEntity == null) return@forEach
            nameTagEntity.destroyForPlayer(player)
        }
    }

    private fun registerListeners() {
        LoggerUtil.log("Registering listeners...")
        val pluginManager: PluginManager = this.plugin.server.pluginManager
        pluginManager.registerEvents(Connections(this), this.plugin)
        pluginManager.registerEvents(PlayerSneak(this), this.plugin)
        pluginManager.registerEvents(PlayerDeath(this), this.plugin)
        pluginManager.registerEvents(PlayerTeleport(this), this.plugin)
        pluginManager.registerEvents(WorldTeleport(this), this.plugin)
    }

    private fun loadTasks() {
        this.plugin.server.scheduler.runTaskTimerAsynchronously(this.plugin, UpdateTagTask(this), 0, 20)
    }

    private fun loadPlayers() {
        Nametag.schedulerUtil.runTaskAsync {
            this.plugin.server.onlinePlayers.forEach { player ->
                val nameTagEntity = NametagEntity(player)
                nameTagEntity.spawn(true)
                this.nametagData.addPlayerNametag(player, nameTagEntity)

                val playerWorld = player.world

                playerWorld.players.forEach { worldPlayer ->
                    if (player.uniqueId == worldPlayer.uniqueId) return@forEach
                    nameTagEntity.spread(worldPlayer)
                }
            }
        }
    }

    private fun registerCommands() {
        this.plugin.getCommand("nametag")?.setExecutor(NametagCommand())
    }

    companion object {
        // Static-Access
        private lateinit var handler: MainHandler

        fun getHandler(): MainHandler {
            return handler
        }
    }
}
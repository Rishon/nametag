package systems.rishon.nametag

import io.papermc.paper.ServerBuildInfo
import org.bukkit.plugin.java.JavaPlugin
import systems.rishon.api.paper.runnable.SchedulerUtil
import systems.rishon.common.IPacket
import systems.rishon.common.MCVersions
import systems.rishon.nametag.handler.FileHandler
import systems.rishon.nametag.handler.IHandler
import systems.rishon.nametag.handler.MainHandler

class Nametag : JavaPlugin() {

    // Handlers
    private lateinit var handlers: MutableList<IHandler>

    // Packet version
    lateinit var packetManager: IPacket

    override fun onEnable() {
        plugin = this
        handleServerVersion()

        schedulerUtil = SchedulerUtil(this)

        registerHandlers()
        this.logger.info("${this.name} has been enabled!")
    }

    override fun onDisable() {
        this.handlers.forEach { it.end() }
        this.logger.info("${this.name} has been disabled!")
    }

    private fun registerHandlers() {
        this.handlers = mutableListOf(FileHandler(this), MainHandler(this))
        this.handlers.forEach {
            this.logger.info("Initializing handler: ${it.javaClass.simpleName}")
            it.init()
            this.logger.info("Handler initialized: ${it.javaClass.simpleName}")
        }
    }

    private fun handleServerVersion() {
        try {
            val build: ServerBuildInfo = ServerBuildInfo.buildInfo()
            val versionId = build.minecraftVersionId()
            when (versionId) {
                "1.20.6" -> this.packetManager = MCVersions.getV1_20_6()
                "1.21" -> this.packetManager = MCVersions.getV1_21()
                else -> {
                    this.logger.severe("Unsupported server version: $versionId")
                    this.server.pluginManager.disablePlugin(this)
                }
            }

            this.logger.info("Detected server version: $versionId")
        } catch (e: Exception) {
            e.printStackTrace()
            this.logger.severe("Failed to load packet manager!")
            this.server.pluginManager.disablePlugin(this)
        }
    }

    companion object {
        lateinit var plugin: Nametag

        // Scheduler Util
        lateinit var schedulerUtil: SchedulerUtil

        // Packet Manager
        fun getPacketManager(): IPacket {
            return plugin.packetManager
        }
    }
}
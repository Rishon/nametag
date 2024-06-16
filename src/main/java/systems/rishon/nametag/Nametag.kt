package systems.rishon.nametag

import org.bukkit.plugin.java.JavaPlugin
import systems.rishon.nametag.handler.IHandler
import systems.rishon.nametag.handler.MainHandler

class Nametag : JavaPlugin() {

    // Handlers
    private val handlers: MutableList<IHandler> = mutableListOf()

    override fun onEnable() {
        plugin = this
        registerHandlers()
        this.logger.info("${this.name} has been enabled!")
    }

    override fun onDisable() {
        this.handlers.forEach { it.end() }
        this.logger.info("${this.name} has been disabled!")
    }

    private fun registerHandlers() {
        this.handlers.add(MainHandler(this))
        this.handlers.forEach { it.init() }
    }

    companion object {
        lateinit var plugin: Nametag
    }
}
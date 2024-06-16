package systems.rishon.nametag

import org.bukkit.plugin.java.JavaPlugin
import systems.rishon.api.paper.runnable.SchedulerUtil
import systems.rishon.nametag.handler.FileHandler
import systems.rishon.nametag.handler.IHandler
import systems.rishon.nametag.handler.MainHandler

class Nametag : JavaPlugin() {

    // Handlers
    private lateinit var handlers: MutableList<IHandler>

    override fun onEnable() {
        plugin = this
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

    companion object {
        lateinit var plugin: Nametag

        // Scheduler Util
        lateinit var schedulerUtil: SchedulerUtil
    }
}
package systems.rishon.nametag.handler

import org.bukkit.configuration.file.FileConfiguration
import systems.rishon.nametag.Nametag

class FileHandler(private val plugin: Nametag) : IHandler {

    // File Configuration
    private lateinit var config: FileConfiguration

    override fun init() {
        this.config = this.plugin.config
        this.config.options().copyDefaults(true)
        this.plugin.saveDefaultConfig()
        this.plugin.saveConfig()
    }

    override fun end() {

    }

    fun reloadConfig() {
        // Reload the config
        this.config = this.plugin.config
        this.plugin.reloadConfig()
        this.plugin.saveConfig()
    }
}
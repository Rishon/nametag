package systems.rishon.nametag.handler

import org.bukkit.configuration.file.FileConfiguration
import systems.rishon.nametag.Nametag

class FileHandler(private val plugin: Nametag) : IHandler {

    // File Configuration
    private lateinit var config: FileConfiguration

    // Config settings
    var hideDefaultNametags: Boolean = false
    var nametagsFormat: List<String> = listOf()
    var nametagsHeight: Double = 0.0
    var textOpacity: Double = 0.0

    override fun init() {
        handler = this;
        this.config = this.plugin.config
        this.config.options().copyDefaults(true)
        this.plugin.saveDefaultConfig()
        this.plugin.saveConfig()

        // Load config settings
        loadConfigSettings()
    }

    override fun end() {}

    private fun loadConfigSettings() {
        this.hideDefaultNametags = this.config.getBoolean("hide-default-nametags")
        this.nametagsFormat = this.config.getStringList("nametag-format.lines").reversed()
        this.nametagsHeight = this.config.getDouble("nametag-format.line-height")
        this.textOpacity = this.config.getDouble("nametag-format.text-opacity-on-sneak")
    }

    fun reloadConfig() {
        // Reload the config
        this.plugin.reloadConfig()
        this.plugin.saveConfig()
        this.config = this.plugin.config

        // Load config settings
        loadConfigSettings()
    }

    companion object {
        // Static-Access
        lateinit var handler: FileHandler
    }
}
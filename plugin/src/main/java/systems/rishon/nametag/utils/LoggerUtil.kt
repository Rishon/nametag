package systems.rishon.nametag.utils

import systems.rishon.nametag.Nametag

object LoggerUtil {

    // Plugin
    private val plugin: Nametag = Nametag.plugin

    @JvmStatic
    fun log(message: String) {
        this.plugin.logger.info(message)
    }

    @JvmStatic
    fun error(message: String) {
        this.plugin.logger.severe(message)
    }

    @JvmStatic
    fun warn(message: String) {
        this.plugin.logger.warning(message)
    }

}
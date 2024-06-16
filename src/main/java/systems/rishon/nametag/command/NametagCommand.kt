package systems.rishon.nametag.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import systems.rishon.api.paper.color.ColorUtil
import systems.rishon.api.paper.color.Colors
import systems.rishon.nametag.handler.FileHandler

class NametagCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        p1: Command,
        p2: String,
        args: Array<out String>?
    ): Boolean {

        if (args == null) return false

        if (args.isEmpty()) {
            sender.sendMessage(ColorUtil.translate("Command Usage: /nametag <reload>", Colors.INFO))
            return true
        }

        when (args[0]) {
            "reload" -> {
                // Reload the config
                FileHandler.handler.reloadConfig()
                sender.sendMessage(ColorUtil.translate("Config reloaded!", Colors.SUCCESS))
            }
        }

        return true
    }
}
package net.justminecraft.plots.commands;

import net.justminecraft.plots.JustPlots;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PluginInfoCommand extends SubCommand {

    public PluginInfoCommand() {
        super("/p plugin", "Get info about this plugin", "plugin", "version");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        JavaPlugin plugin = JustPlots.getPlugin();
        sender.sendMessage(ChatColor.AQUA + " --------- " + ChatColor.WHITE + plugin.getName() + " v" + plugin.getDescription().getVersion() + ChatColor.AQUA + " --------- ");
        sender.sendMessage(ChatColor.DARK_AQUA + "Authors: " + ChatColor.GRAY + StringUtils.join(plugin.getDescription().getAuthors(), ","));
        sender.sendMessage(ChatColor.DARK_AQUA + "Website: " + ChatColor.GRAY + plugin.getDescription().getWebsite());

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p plugin takes no arguments
    }

}

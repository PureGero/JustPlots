package net.justminecraft.plots.commands;

import net.justminecraft.plots.listeners.WorldEditListener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class WeanywhereCommand extends SubCommand {

    private static WorldEditListener worldEditListener = null;

    private static HashSet<UUID> weanywherePlayers = new HashSet<>();

    public static void setWorldEditListener(WorldEditListener worldEditListener) {
        WeanywhereCommand.worldEditListener = worldEditListener;
    }

    public static boolean isWeanywhere(Player player) {
        return weanywherePlayers.contains(player.getUniqueId()) && player.hasPermission(getPerm());
    }

    public WeanywhereCommand() {
        super("/p weanywhere", "Toggle using worldedit anywhere", "weanywhere", "wea");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run that command");
            return false;
        }

        if (weanywherePlayers.remove(((Player) sender).getUniqueId())) {
            sender.sendMessage(ChatColor.AQUA + "You can no longer use world edit anywhere");
        } else {
            weanywherePlayers.add(((Player) sender).getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "You can now use world edit anywhere");
        }

        if (worldEditListener != null) {
            worldEditListener.setup((Player) sender);
        }

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p weanywhere takes no arguments
    }

    @Override
    public String getPermission() {
        return getPerm();
    }

    private static String getPerm() {
        return "justplots.weanywhere";
    }

}

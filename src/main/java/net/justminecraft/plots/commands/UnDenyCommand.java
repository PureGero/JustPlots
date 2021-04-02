package net.justminecraft.plots.commands;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.events.PlotPlayerUnDenyEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnDenyCommand extends SubCommand {
    public UnDenyCommand() {
        super("/p undeny <player>", "Allow a denied player back into your plot", "undeny");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        Plot plot = JustPlots.getPlotAt((Player) sender);

        if (plot == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        if (!plot.isOwner((Player) sender) && !sender.hasPermission("justplots.deny.other")) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " owns that plot");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return false;
        }

        OfflinePlayer toUnDeny = Bukkit.getOfflinePlayer(args[0]);

        if (!plot.isDenied(toUnDeny)) {
            sender.sendMessage(ChatColor.RED + toUnDeny.getName() + " has not been denied from that plot");
            return false;
        }

        if (plot.isOwner(toUnDeny)) {
            sender.sendMessage(ChatColor.RED + toUnDeny.getName() + " is the owner of that plot");
            return false;
        }

        PlotPlayerUnDenyEvent event = new PlotPlayerUnDenyEvent(plot, toUnDeny.getUniqueId());

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            throw new RuntimeException("Event was cancelled");
        }

        plot.unDenyPlayer(toUnDeny.getUniqueId());

        String whos = plot.isOwner((Player) sender) ? "your" : JustPlots.getUsername(plot.getOwner()) + "'s";

        sender.sendMessage(ChatColor.GREEN + "Succesfully undenied " + toUnDeny.getName() + " from " + whos + " plot");

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    tabCompletion.add(player.getName());
                }
            }
        }
    }

}

package just.plots.commands;

import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RemoveCommand extends SubCommand {

    public RemoveCommand() {
        super("/p remove <player>", "Remove a player from your plot", "remove", "untrust", "unadd", "r");
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

        if (!plot.isOwner((Player) sender) && !sender.hasPermission("justplots.add.other")) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " owns that plot");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return false;
        }

        OfflinePlayer toRemove = Bukkit.getOfflinePlayer(args[0]);

        if (!plot.isAdded(toRemove)) {
            sender.sendMessage(ChatColor.RED + toRemove.getName() + " has not been added to that plot");
            return false;
        }

        if (plot.isOwner(toRemove)) {
            sender.sendMessage(ChatColor.RED + toRemove.getName() + " is the owner of that plot");
            return false;
        }

        plot.removePlayer(toRemove.getUniqueId());

        String whos = plot.isOwner((Player) sender) ? "your" : JustPlots.getUsername(plot.getOwner()) + "'s";

        sender.sendMessage(ChatColor.GREEN + "Succesfully removed " + toRemove.getName() + " from " + whos + " plot");

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

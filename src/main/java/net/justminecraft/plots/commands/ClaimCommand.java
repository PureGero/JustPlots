package net.justminecraft.plots.commands;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.PlotId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClaimCommand extends SubCommand {

    public ClaimCommand() {
        super("/p claim [player]", "Claim a plot", "claim");
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

        OfflinePlayer claimAs = (OfflinePlayer) sender;

        if (args.length >= 1 && sender.hasPermission("justplots.claim.other")) {
            claimAs = Bukkit.getOfflinePlayer(args[0]);
        }

        String world = ((Player) sender).getWorld().getName();
        PlotId id = JustPlots.getPlotIdAt((Player) sender);

        if (id == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        Plot plot = JustPlots.getPlot(world, id);

        if (plot != null) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " already owns that plot");
            return false;
        }

        int maxPlots = JustPlots.getMaxPlots(sender);

        if (maxPlots < Integer.MAX_VALUE && JustPlots.getPlotsInWorld((Player) sender, ((Player) sender).getWorld()).size() >= maxPlots) {
            sender.sendMessage(ChatColor.RED + "You have reached your plot limit of " + maxPlots);
            return false;
        }

        plot = JustPlots.claimPlot(world, id, claimAs.getUniqueId());

        sender.sendMessage(ChatColor.GREEN + "Succesfully claimed plot " + plot + (claimAs != sender ? " for " + claimAs.getName() : ""));

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

    @Override
    public String getPermission() {
        return "justplots.claim";
    }

}

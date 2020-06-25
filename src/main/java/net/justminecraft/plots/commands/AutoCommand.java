package net.justminecraft.plots.commands;

import io.papermc.lib.PaperLib;
import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.PlotId;
import net.justminecraft.plots.util.PaperUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class AutoCommand extends SubCommand {

    public AutoCommand() {
        super("/p auto", "Claim the next free plot", "auto");
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

        int maxPlots = JustPlots.getMaxPlots(sender);

        if (maxPlots < Integer.MAX_VALUE && JustPlots.getPlotsInWorld((Player) sender, ((Player) sender).getWorld()).size() >= maxPlots) {
            sender.sendMessage(ChatColor.RED + "You have reached your plot limit of " + maxPlots);
            return false;
        }

        String world = ((Player) sender).getWorld().getName();
        PlotId id = JustPlots.getPlotWorld(world).nextAutoClaimPlot();

        if (id == null) {
            sender.sendMessage(ChatColor.RED + "Could not find a plot to claim");
            return false;
        }

        Plot plot = JustPlots.claimPlot(world, id, ((Player) sender).getUniqueId());

        sender.sendMessage(ChatColor.GREEN + "Succesfully claimed plot " + plot);

        PaperUtil.teleportAsync((Entity) sender, plot.getHome(), PlayerTeleportEvent.TeleportCause.COMMAND);

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
        return "justplots.auto";
    }

}

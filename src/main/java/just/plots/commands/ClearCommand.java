package just.plots.commands;

import just.plots.JustPlots;
import just.plots.Plot;
import just.plots.events.PlotClearEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class ClearCommand extends SubCommand {

    public ClearCommand() {
        super("/p clear", "Clear your plot", "clear");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        if (!sender.hasPermission("justplots.clear")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run that command");
            return false;
        }

        Plot plot = JustPlots.getPlotAt((Entity) sender);

        if (plot == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        if (!plot.isOwner((Player) sender) && !sender.hasPermission("justplots.clear.other")) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " owns that plot");
            return false;
        }

        PlotClearEvent event = new PlotClearEvent(plot, (Player) sender);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            throw new RuntimeException("Event was cancelled");
        }

        plot.reset();

        String name = plot.isOwner((Player) sender) ? "your" : JustPlots.getUsername(plot.getOwner()) + "'s";
        sender.sendMessage(ChatColor.GREEN + "Succesfully cleared " + name
                + " plot at " + plot);

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p clear takes no arguments
    }

}

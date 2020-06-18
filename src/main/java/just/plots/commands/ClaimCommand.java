package just.plots.commands;

import just.plots.JustPlots;
import just.plots.Plot;
import just.plots.PlotId;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ClaimCommand extends SubCommand {

    public ClaimCommand() {
        super("/p claim", "Claim a plot", "claim");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        String world = ((Player) sender).getWorld().getName();
        PlotId id = JustPlots.getPlotIdAt((Player) sender);

        if (id == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        Plot plot = JustPlots.getPlot(world, id);

        if (plot != null) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " already owns that plot!");
            return false;
        }

        plot = JustPlots.claimPlot(world, id, ((Player) sender).getUniqueId());

        sender.sendMessage(ChatColor.GREEN + "Succesfully claimed plot " + plot);

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p claim takes no arguments
    }

}

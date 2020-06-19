package just.plots.commands;

import io.papermc.lib.PaperLib;
import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MiddleCommand extends SubCommand {

    public MiddleCommand() {
        super("/p middle", "Go to the middle of the plot", "middle", "center", "centre");
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

        PaperLib.teleportAsync((Player) sender, plot.getMiddle());

        sender.sendMessage(ChatColor.AQUA + "Teleported to the middle of the plot");

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p info takes no arguments
    }

}

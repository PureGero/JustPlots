package just.plots.commands;

import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DisposeCommand extends SubCommand {

    public DisposeCommand() {
        super("/p dispose", "Delete a plot without resetting its contents", "dispose");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        if (!sender.hasPermission("justplots.dispose")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run that command");
            return false;
        }

        Plot plot = JustPlots.getPlotAt((Entity) sender);

        if (plot == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        if (!plot.isOwner((Player) sender) && !sender.hasPermission("justplots.dispose,other")) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " owns that plot");
            return false;
        }

        plot.delete();

        String name = plot.isOwner((Player) sender) ? "your" : JustPlots.getUsername(plot.getOwner()) + "'s";
        sender.sendMessage(ChatColor.GREEN + "Succesfully disposed " + name
                + " plot at " + plot);

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p dispose takes no arguments
    }

}

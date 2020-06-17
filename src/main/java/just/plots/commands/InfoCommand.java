package just.plots.commands;

import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InfoCommand extends SubCommand {

    public InfoCommand() {
        super("Get info about the plot", "info", "i");
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

        sender.sendMessage(ChatColor.AQUA + "Plot " + plot.toString() + " belongs to " + JustPlots.getUsername(plot.getOwner()));

        StringBuilder added = new StringBuilder(ChatColor.DARK_AQUA + "Added: " + ChatColor.WHITE);

        Set<UUID> addedUuids = plot.getAdded();

        if (addedUuids.isEmpty()) {
            added.append("No one");
        }

        boolean first = true;
        for (UUID uuid : addedUuids) {
            if (!first) {
                added.append(", ");
            }

            added.append(JustPlots.getUsername(uuid));

            first = false;
        }

        sender.sendMessage(added.toString());

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p info takes no arguments
    }

}

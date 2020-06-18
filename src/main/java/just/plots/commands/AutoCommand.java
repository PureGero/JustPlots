package just.plots.commands;

import io.papermc.lib.PaperLib;
import just.plots.JustPlots;
import just.plots.Plot;
import just.plots.PlotId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

        if (!sender.hasPermission("justplots.auto")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run that command");
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

        PaperLib.teleportAsync((Entity) sender, plot.getHome(), PlayerTeleportEvent.TeleportCause.COMMAND);

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

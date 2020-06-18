package just.plots.commands;

import com.earth2me.essentials.User;
import io.papermc.lib.PaperLib;
import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class VisitCommand extends SubCommand {

    public VisitCommand() {
        super("Vist a plot", "visit", "v", "home", "h", "tp");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        String format = "Teleported to your plot #%d";
        Set<Plot> plots = JustPlots.getPlots((Player) sender);
        Plot plot = null;

        int i = Integer.MIN_VALUE;

        if (args.length >= 2) {
            try {
                i = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "'" + i + "' is an invalid plot number");
                return false;
            }
        }

        if (args.length >= 1) {
            boolean number = false;
            if (i == Integer.MIN_VALUE) {
                try {
                    i = Integer.parseInt(args[0]) - 1;
                    number = true;
                } catch (NumberFormatException ignored) {}
            }

            if (!number || i < 0 || i >= plots.size()) {
                String[] ids = args[0].split(";");

                if (ids.length >= 2) {
                    String world = ((Entity) sender).getWorld().getName();
                    int x, z;
                    if (ids.length >= 3) {
                        world = ids[0];
                        x = Integer.parseInt(ids[1]);
                        z = Integer.parseInt(ids[2]);
                    } else {
                        x = Integer.parseInt(ids[0]);
                        z = Integer.parseInt(ids[1]);
                    }

                    plot = JustPlots.getPlot(world, x, z);
                    format = "Teleported to plot " + plot;

                    if (plot == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid plot '" + args[0] + "'");
                        return false;
                    }
                } else {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

                    Set<Plot> playerPlots = JustPlots.getPlots(player.getUniqueId());

                    if (playerPlots.isEmpty()) {
                        if (i == Integer.MIN_VALUE || args.length >= 2) {
                            sender.sendMessage(ChatColor.RED + player.getName() + " has no plots");
                            return false;
                        }
                    } else {
                        plots = playerPlots;
                        format = "Teleported to " + player.getName() + "'s plot #%d";
                    }
                }
            }
        }

        if (plot == null) {
            if (i == Integer.MIN_VALUE) {
                i = 0;
            }

            if (plots.size() == 0) {
                sender.sendMessage(ChatColor.RED + "You have no plots. Get one with /p auto");
                return false;
            }

            if (i < 0 || i >= plots.size()) {
                sender.sendMessage(ChatColor.RED + "Plot number must be between 1 and " + plots.size());
                return false;
            }

            Iterator<Plot> iterator = plots.iterator();
            for (int j = 0; j < i; j++) iterator.next();
            plot = iterator.next();
        }

        Location location = plot.getHome();

        if (location.getWorld() == null) {
            sender.sendMessage(ChatColor.RED + "The world '" + plot.getWorldName() + "' doesn't exist");
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + String.format(format, i + 1));
        PaperLib.teleportAsync((Entity) sender, location, PlayerTeleportEvent.TeleportCause.COMMAND);

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

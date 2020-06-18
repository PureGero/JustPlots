package just.plots.commands;

import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ListCommand extends SubCommand {

    public ListCommand() {
        super("List your plots", "list", "l", "plots");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {

        String format = "Your plots";
        Set<Plot> plots = sender instanceof Player ? JustPlots.getPlots((Player) sender) : new TreeSet<>();

        World world = null;

        int page = Integer.MIN_VALUE;

        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]) - 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "'" + page + "' is an invalid plot number");
                return false;
            }
        }

        if (args.length >= 2) {
            boolean number = false;
            if (page == Integer.MIN_VALUE) {
                try {
                    page = Integer.parseInt(args[1]) - 1;
                    number = true;
                } catch (NumberFormatException ignored) {}
            }

            if (!number) {
                world = Bukkit.getWorld(args[1]);

                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid world '" + args[1] + "'");
                    return false;
                }
            }
        }

        if (args.length >= 1) {
            boolean number = false;
            if (page == Integer.MIN_VALUE) {
                try {
                    page = Integer.parseInt(args[0]) - 1;
                    number = true;
                } catch (NumberFormatException ignored) {}
            }

            if (!number) {
                if (world != null || (world = Bukkit.getWorld(args[0])) == null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

                    plots = JustPlots.getPlots(player.getUniqueId());

                    if (plots.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + player.getName() + " has no plots");
                        return false;
                    } else {
                        format = player.getName() + "'s plots";
                    }
                }
            }
        }

        if (page == Integer.MIN_VALUE) {
            page = 0;
        }

        if (plots.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You have no plots. Get one with /p auto");
            return false;
        }

        List<Plot> plotList = new ArrayList<>();

        for (Plot plot : plots) {
            if (world == null || plot.getWorldName().equals(world.getName())) {
                plotList.add(plot);
            }
        }

        if (page < 0 || page > (plotList.size() - 1) / 10) {
            sender.sendMessage(ChatColor.RED + "Page number must be between 1 and " + (plotList.size() + 9) / 10);
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + format + (world != null ? " in " + world.getName() : "") + " (" + (page + 1) + "/" + (plotList.size() + 9) / 10 + ")");

        for (int i = page * 10; i < page * 10 + 10 && i < plotList.size(); i++) {
            Plot plot = plotList.get(i);
            sender.sendMessage(ChatColor.DARK_AQUA + "" + (i + 1) + ") " + plot + ": " + ChatColor.WHITE + plot.getCreationDate());
        }

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

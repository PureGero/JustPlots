package net.justminecraft.plots.commands;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.PlotInfoEntry;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InfoCommand extends SubCommand {

    private static ArrayList<PlotInfoEntry> entries = new ArrayList<>();

    public static void addEntry(PlotInfoEntry plotInfoEntry) {
        entries.add(plotInfoEntry);
    }

    static {
        new PlotInfoEntry("Created on") {
            @Override
            public BaseComponent[] getValue(@NotNull Plot plot) {
                return new ComponentBuilder(plot.getCreationDate()).create();
            }
        };

        new PlotInfoEntry("Added players") {
            @Override
            public BaseComponent[] getValue(@NotNull Plot plot) {
                ComponentBuilder builder = new ComponentBuilder();

                Set<UUID> addedUuids = plot.getAdded();

                if (addedUuids.isEmpty()) {
                    builder.append("No one").color(ChatColor.GRAY);
                }

                boolean first = true;
                for (UUID uuid : addedUuids) {
                    if (!first) {
                        builder.append(", ");
                    }

                    builder.append(JustPlots.getUsername(uuid));

                    first = false;
                }

                return builder.create();
            }
        };
    }

    public InfoCommand() {
        super("/p info", "Get info about the plot", "info", "i");
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

        for (PlotInfoEntry entry : entries) {
            BaseComponent[] value = entry.getValue(plot);

            if (value != null) {
                sender.spigot().sendMessage(new ComponentBuilder(entry.getKey() + ": ").color(ChatColor.DARK_AQUA)
                        .append(new ComponentBuilder().color(ChatColor.WHITE).append(value).create()).create());
            }
        }

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p info takes no arguments
    }

}

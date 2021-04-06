package net.justminecraft.plots.listeners;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.events.PlotEnterEvent;
import net.justminecraft.plots.events.PlotExitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    public PlayerListener(JustPlots plots) {
        plots.getServer().getPluginManager().registerEvents(this, plots);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!hasMovedBlock(e.getFrom(), e.getTo())) {
            return;
        }

        Plot from = JustPlots.getPlotAt(e.getFrom());
        Plot to = JustPlots.getPlotAt(e.getTo());

        if (!e.isCancelled() && to != null && to.isDenied(e.getPlayer().getUniqueId())) {
            e.setTo(to.getHome());
        }

        if (from != to) {
            if (from != null) {
                PlotExitEvent exitEvent = new PlotExitEvent(from, e.getPlayer());

                Bukkit.getServer().getPluginManager().callEvent(exitEvent);

                if (exitEvent.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (to != null) {
                PlotEnterEvent enterEvent = new PlotEnterEvent(to, e.getPlayer());

                Bukkit.getServer().getPluginManager().callEvent(enterEvent);

                if (enterEvent.isCancelled() || to.isDenied(e.getPlayer().getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (!hasMovedBlock(e.getFrom(), e.getTo())) {
            return;
        }

        Plot from = JustPlots.getPlotAt(e.getFrom());
        Plot to = JustPlots.getPlotAt(e.getTo());

        if (!e.isCancelled() && to != null && to.isDenied(e.getPlayer().getUniqueId())) {
            e.setTo(to.getHome());
        }

        if (from != to) {
            if (from != null) {
                PlotExitEvent exitEvent = new PlotExitEvent(from, e.getPlayer());

                Bukkit.getServer().getPluginManager().callEvent(exitEvent);

                if (exitEvent.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (to != null) {
                PlotEnterEvent enterEvent = new PlotEnterEvent(to, e.getPlayer());

                Bukkit.getServer().getPluginManager().callEvent(enterEvent);

                if (enterEvent.isCancelled()) {
                    // Can't enter the plot, teleport them to the home instead
                    e.setTo(to.getHome());
                }
            }
        }
    }

    private boolean hasMovedBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ();
    }
}

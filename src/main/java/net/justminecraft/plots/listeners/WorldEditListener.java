package net.justminecraft.plots.listeners;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.commands.WeanywhereCommand;
import net.justminecraft.plots.events.PlotDeletedEvent;
import net.justminecraft.plots.events.PlotEnterEvent;
import net.justminecraft.plots.events.PlotPlayerAddEvent;
import net.justminecraft.plots.events.PlotPlayerRemoveEvent;
import net.justminecraft.plots.util.JustPlotsRegionMask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class WorldEditListener implements Listener {

    private WorldEditPlugin worldEditPlugin = null;

    public WorldEditListener(JustPlots plots) {
        for (Plugin plugin : plots.getServer().getPluginManager().getPlugins()) {
            if (plugin instanceof WorldEditPlugin) {
                // Future proof incase the plugin name changes, or another plugin supports the worldedit api
                worldEditPlugin = (WorldEditPlugin) plugin;
            }
        }

        if (worldEditPlugin != null) {
            WeanywhereCommand.setWorldEditListener(this);

            plots.getServer().getPluginManager().registerEvents(this, plots);

            for (Player player : Bukkit.getOnlinePlayers()) {
                setup(player);
            }
        }
    }

    public void setup(Player player) {
        if (JustPlots.isPlotWorld(player.getWorld())) {
            Plot plot = JustPlots.getPlotAt(player);

            if (WeanywhereCommand.isWeanywhere(player)) {
                removeMask(player);
            } else if (plot != null && plot.isAdded(player)) {
                setMask(player, plot);
            } else {
                setMask(player, null);
            }
        }
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        setup(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (JustPlots.isPlotWorld(e.getFrom()) && !JustPlots.isPlotWorld(e.getPlayer().getWorld())
                && isJustPlotsMask(e.getPlayer())) {
            removeMask(e.getPlayer());
        } else {
            setup(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlotEnter(PlotEnterEvent e) {
        if (!WeanywhereCommand.isWeanywhere(e.getPlayer()) && e.getPlot().isAdded(e.getPlayer())) {
            setMask(e.getPlayer(), e.getPlot());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlotPlayerAdded(PlotPlayerAddEvent e) {
        if (e.getPlayer() != null && JustPlots.getPlotAt(e.getPlayer()) == e.getPlot()) {
            setMask(e.getPlayer(), e.getPlot());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlotPlayerRemoved(PlotPlayerRemoveEvent e) {
        if (e.getPlayer() != null && getMaskPlot(e.getPlayer()) == e.getPlot()) {
            Bukkit.getScheduler().runTask(JustPlots.getPlugin(), () -> setup(e.getPlayer()));
        }
    }

    @EventHandler
    public void onPlotPlayerRemoved(PlotDeletedEvent e) {
        Player player;

        for (UUID added : e.getPlot().getAdded()) {
            if ((player = Bukkit.getPlayer(added)) != null && getMaskPlot(player) == e.getPlot()) {
                setup(player);
            }
        }

        if ((player = Bukkit.getPlayer(e.getPlot().getOwner())) != null && getMaskPlot(player) == e.getPlot()) {
            setup(player);
        }
    }

    private boolean isJustPlotsMask(Player player) {
        return worldEditPlugin.getSession(player).getMask() instanceof JustPlotsRegionMask;
    }

    private Plot getMaskPlot(Player player) {
        Mask mask = worldEditPlugin.getSession(player).getMask();
        return mask instanceof JustPlotsRegionMask ? ((JustPlotsRegionMask) mask).getPlot() : null;
    }

    private void setMask(Player player, Plot plot) {
        Location bottom, top;

        if (plot != null) {
            bottom = plot.getBottom();
            top = plot.getTop();
        } else {
            bottom = top = new Location(player.getWorld(), 0, -1, 0);
        }

        LocalSession localSession = worldEditPlugin.getSession(player);
        World world = localSession.getSelectionWorld();

        BlockVector3 pos1 = BlockVector3.at(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ());
        BlockVector3 pos2 = BlockVector3.at(top.getBlockX(), top.getBlockY(), top.getBlockZ());

        CuboidRegion cuboidRegion = new CuboidRegion(world, pos1, pos2);
        RegionMask regionMask = new JustPlotsRegionMask(cuboidRegion, plot);
        localSession.setMask(regionMask);
    }

    private void removeMask(Player player) {
        LocalSession localSession = worldEditPlugin.getSession(player);
        localSession.setMask(null);
    }
}
